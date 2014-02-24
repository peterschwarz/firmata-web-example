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



(defn create-app-routes
  [board]
  (defroutes app-routes
    (GET "/" []
         (file-response "index.html" {:root "resources/public"}))
    (GET "/async" [] (create-async-handler board)) ;; asynchronous(long polling)
    (route/resources "/") ; {:root "resources"})
    (route/not-found "Not Found"))

  app-routes)


(defn app [board]
  (-> (handler/site (create-app-routes board))
      (wrap-timbre {})))

(defn -main [& args]

  (let [port-name (first args)
        board (open-board port-name)
        version (<!! (:channel board))
        firmware (<!! (:channel board))
        receiver (pull-events board)]
    (info "Connected to board")
    (info "Firmata version: " (:version version))
    (info "Firmaware name: " (:name firmware))

    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn []
                                 (info "Disconnecting")
                                 (stop-events board receiver)
                                 (close! board))))

    (run-server (app board) {:port 3000})))

