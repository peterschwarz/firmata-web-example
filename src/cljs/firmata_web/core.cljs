(ns firmata-web.core
  (:require
   [domina :as dom]
   [domina.css :as css]
   [domina.events :as events]
   [cljs.core.async :refer [chan <! >! put! timeout]]
   [cljs.reader :as reader])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))

(def receive (chan))
(def alert-view (chan))

(def ws-url "ws://localhost:3000/async")
(def ws (new js/WebSocket ws-url))

(defn connect-client []
  ; Not sure why (.-onopen ws #(...)) doesn't work here
  (aset ws "onopen" #(.send ws #{:new-client})))

(defn sensor-template [event]
  (if (= :connected (:type event))
    "Connected!"
    (str (:value event))
    ))

(defn reading []
  (css/sel ".reading"))

(defn receive-readings []
  (go
   (loop [data (<! receive)]
     (let [templated-data (sensor-template data)
           reading (reading)]
       (dom/set-html! reading templated-data)
       (dom/add-class! reading "new")
       (>! alert-view reading))
     (recur (<! receive)))))

(defn highlight-new-reading []
  (go
   (loop [alert-reading (<! alert-view)]
     (<! (timeout 200))
     (dom/remove-class! alert-reading "new")
     (recur (<! alert-view))
     )))

(defn make-receiver []
  (set! (.-onmessage ws) #(put! receive (reader/read-string (.-data %))))
  (receive-readings)
  (highlight-new-reading))

(defn init! []
  (connect-client)
  (make-receiver))

(def on-load
  (set! (.-onload js/window) init!))
