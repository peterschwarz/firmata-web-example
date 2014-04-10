(ns firmata-web.handler
  (:require
   [compojure.core :refer :all]
   [clojure.edn :as edn]
   [org.httpkit.server :refer [run-server]]
   [firmata.core :refer [open-board close!]]
   [firmata-web.middleware :refer [wrap-timbre]]
   [firmata-web.async :refer [create-async-handler pull-events stop-events]]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [clojure.core.async :refer [<!!]]
   [ring.middleware.reload :as reload]

   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]
   [ring.util.response :refer [file-response]]))

(def board (atom nil))

(defroutes app-routes
  (GET "/" []
       (file-response "index.html" {:root "resources/public"}))
  (GET "/async" [] (create-async-handler @board)) ;; asynchronous(long polling)
  (route/resources "/") ; {:root "resources"})
  (route/not-found "Not Found"))


(defn app [board]
  (-> (handler/site (app-routes))
      (wrap-timbre {})))

(defn -main [& args]

  (let [port-name (first args)
        _ (reset! board (open-board port-name))
        receiver (pull-events @board)]
    (info "Connected to board")

    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn []
                                 (info "Disconnecting")
                                 (stop-events @board receiver)
                                 (close! @board))))

    (run-server (app board) {:port 3000})))

