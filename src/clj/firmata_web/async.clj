(ns firmata-web.async
  (:require [org.httpkit.server :as httpkit]
            [clojure.edn :as edn]
            [clojure.core.async :as a :refer [>! <!]]
            [clj-time.local :as lt]
            [firmata.core :as firmata]
            [firmata.receiver]
            [taoensso.timbre :refer [trace debug info warn error fatal spy with-log-level]]))

(def NOTIFICATION_PORT 10)
(def ANALOG_PORT 0)

(def channels (atom #{}))

(def user-connected (a/chan))
(def sensor-reading (a/chan))

(def last-sensor-reading (atom nil))

(defn- send! [c event]
  (httpkit/send!
     c (pr-str event)))

(defn update-channels!
  [event]
  (doseq [c @channels]
    (send! c event)))

(defn- add-channel!
  [channel]
  (if (not (seq (filter #(= % channel) @channels)))
    (swap! channels conj channel)))


(defn- connect
  [channel]
  (fn [raw]
    (let [data (edn/read-string raw)]
      (info "WebSocket: " data)

      (a/go
       (>! user-connected :connected))
      (add-channel! channel)
      (send! channel (if-let [last-reading @last-sensor-reading]
                       last-reading
                       {:type :connected})))))

(defn- disconnect
  [channel]
  (fn [status]
    (info "Closing socket: " channel " with status " status)
    (swap! channels disj channel)))

(defn run-new-user
  [board]
  (a/go
   (loop [connected (<! user-connected)]
     (when connected
       (debug "New user connected")
       (firmata/set-digital board NOTIFICATION_PORT :high)
       (<! (a/timeout 250))
       (firmata/set-digital board NOTIFICATION_PORT :low)
       (<! (a/timeout 250))
       (recur (<! user-connected))))))

(defn run-channel-updates
  []
  (a/go
   (loop [event (<! sensor-reading)]
     (when event
       (reset! last-sensor-reading event)
       (update-channels! event)
       (recur (<! sensor-reading))))))

(defn create-async-handler [board]

  (run-new-user board)

  (run-channel-updates)

  (fn [request]
    (httpkit/with-channel
     request channel
     (if (httpkit/websocket? channel)
       (do
         (httpkit/on-close channel (disconnect channel))
         (httpkit/on-receive channel (connect channel)))
       (httpkit/send! channel {:status 200
                               :headers {"Content-Type" "text/plain"}
                               :body    "Long polling?"})))))

(defn pull-events
  [board]
  (let [receiver (firmata.receiver/on-analog-event board ANALOG_PORT #(a/go (a/>! sensor-reading %)))]
    (debug "Enabling analog event reporting on A0")
    (firmata/enable-analog-in-reporting board ANALOG_PORT true)
    (debug "Ready to receive sensor readings")
    receiver))

(defn stop-events [board receiver]
  (firmata.receiver/stop-receiver! receiver)
  (firmata/enable-analog-in-reporting board ANALOG_PORT false))
