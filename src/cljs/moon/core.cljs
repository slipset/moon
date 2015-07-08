(ns moon.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [moon.dom :refer [set-html! by-id listen beep ping]]
            [cljs.moon.components :as components]
            [cljs.moon.workout :as workout]            
            [om.core :as om]              
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :refer-macros [defroute]]
            [cljs.core.async :refer
             [<! >!  timeout onto-chan chan put! take! alts! close!]])
  (:import goog.History))

(defonce app-state (atom {:running-workout false
                          :total-duration 0
                          :current-exercise {}
                          :config {
                                   :flux (chan)
                                   }
                          :workout nil
                          :workouts workout/workouts}))

(defmulti play :state)

(defmethod play :almost [_]
  (beep))

(defmethod play :done [_]
  (ping))

(defmethod play :default [_])

(defn- root-cursor []
  (om/root-cursor app-state))

(defn start-workout [flux workout]
  (om/update! (root-cursor) [:running-workout] true)
  (om/update! (root-cursor) [:current-exercise] (assoc (first workout)
                                                       :remaining 10 :activity :ready))
  (workout/run flux workout))

#_(defroute  "/workout" []
    (.log js/console "starting workout")
    (start-workout))

#_(defroute "/" []
    (.log js/console "showing root")
    (show-root))

(defmulti event-handlers :event)

(defmethod event-handlers :choose-workout [event]
  (let [workout (get-in @app-state [:workouts (:workout event)])
        total-duration (workout/total-duration workout)]
    (om/transact! (root-cursor) (fn [s] (merge s {:workout workout
                                                  :running-workout false
                                                  :total-duration total-duration
                                                  :remaining total-duration})))))
(defmethod event-handlers :start-workout [event]
  (start-workout (get-in @app-state [:config :flux]) (:workout @app-state)))

(defmethod event-handlers :home [event]
  (om/transact! (root-cursor) (fn [s]
                                (assoc s :workout nil :running-workout false))))

(defmethod event-handlers :play [event]
  (play event))

(defmethod event-handlers :count-down [event]
  (play event)
  (om/transact! (root-cursor) [:current-exercise :remaining] dec))

(defmethod event-handlers :start-exercise [event]
  (om/transact! (root-cursor) :workout clojure.core/rest))

(defmethod event-handlers :update-current [event]
  (om/transact! (root-cursor) (fn [s]
                                (assoc s :remaining (dec (:remaining s))
                                       :current-exercise (:current-exercise event)))))

(defn handle-events [flux workouts]
  (go-loop []
    (event-handlers (<! flux))
    (recur)))

(defn main []
  (let [config (:config @app-state)]
    (om/root components/app app-state {:target (by-id "app")
                                       :shared {:config config}})
    (handle-events (:flux config) (:workouts @app-state)))
  
  #_(let [h (History.)]
      (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
      (doto h (.setEnabled true)
            (.setToken "/"))))

(defn on-reload []
  (close! workout/wall-clock))

