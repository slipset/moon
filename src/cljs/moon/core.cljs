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
                          :workouts {
                                     :transgression [
                                                     {:title "Hang"
                                                      :holds []
                                                      :duration 10
                                                      :repeat 2
                                                      :rest 30
                                                      }
                                                     {:title "Rest"
                                                      :duration 0
                                                      :rest 180
                                                      :repeat 1
                                                      }
                                                     {:title "Hang"
                                                      :holds []
                                                      :duration 10
                                                      :repeat 2
                                                      :rest 30
                                                      }
                                                     {:title "Rest"
                                                      :duration 0
                                                      :rest 180
                                                      :repeat 1
                                                      }
                                                     {:title "Hang"
                                                      :holds []
                                                      :duration 10
                                                      :repeat 2
                                                      :rest 30
                                                      }
                                                     {:title "Rest"
                                                      :duration 0
                                                      :rest 180
                                                      :repeat 1
                                                      }
                                                     {:title "Hang"
                                                      :holds []
                                                      :duration 10
                                                      :repeat 2
                                                      :rest 30
                                                      }
                                                     ]
                                     :short [
                                             {:title "Double armed dead hang on front three fingers open handed"
                                              :holds [5]
                                              :duration 6
                                              :rest 120
                                              :repeat 3
                                              }
                                             {:title "Double armed dead hang on middle two fingers open handed"
                                              :holds [5]
                                              :duration 6
                                              :rest 90
                                              :repeat 2
                                              }
                                             {:title "Hang open handed on three fingers on one arm, decrease resistance as required until hang can be completed."
                                              :holds [2,5]
                                              :duration 6
                                              :rest 60
                                              :repeat 6
                                              }
                                             {:title "Rest"
                                              :duration 0
                                              :rest 240
                                              :repeat 1
                                              }
                                             {:title "Double armed hang on full crimp position, on a first joint edge"
                                              :holds [1,5]
                                              :duration 6
                                              :rest 90
                                              :repeat 3
                                              }
                                             {:title "Double armed hang on full crimp, this time slightly smaller than the last set, i.e., _ of the the finger tip."
                                              :holds [3,4]
                                              :duration 6
                                              :rest 90
                                              :repeat 3
                                              }
                                             {:title "Single arm hang on 1st joint edge, decrease resistance if required until hang can be completed."
                                              :holds [1, 4, 5]
                                              :duration 6
                                              :rest 60
                                              :repeat 6
                                              }]
                                     :moon [
                                            {:title "Double armed dead hang on front three fingers open handed"
                                             :holds [5]
                                             :duration 6
                                             :rest 120
                                             :repeat 3
                                             }
                                            {:title "Double armed dead hang on middle two fingers open handed"
                                             :holds [5]
                                             :duration 6
                                             :rest 90
                                             :repeat 2
                                             }
                                            {:title "Hang open handed on three fingers on one arm, decrease resistance as required until hang can be completed."
                                             :holds [2,5]
                                             :duration 6
                                             :rest 60
                                             :repeat 6
                                             }
                                            {:title "Rest"
                                             :duration 0
                                             :rest 240
                                             :repeat 1
                                             }
                                            {:title "Double armed hang on full crimp position, on a first joint edge"
                                             :holds [1,5]
                                             :duration 6
                                             :rest 90
                                             :repeat 3
                                             }
                                            {:title "Double armed hang on full crimp, this time slightly smaller than the last set, i.e., _ of the the finger tip."
                                             :holds [3,4]
                                             :duration 6
                                             :rest 90
                                             :repeat 3
                                             }
                                            {:title "Single arm hang on 1st joint edge, decrease resistance if required until hang can be completed."
                                             :holds [1, 4, 5]
                                             :duration 6
                                             :rest 60
                                             :repeat 6
                                             }
                                            {:title "Rest"
                                             :duration 0
                                             :rest 240
                                             :repeat 1
                                             }
                                            {:title "Single arm lock off at 90 degrees"
                                             :holds [1, 5]
                                             :duration 8
                                             :rest 120
                                             :repeat 6
                                             }
                                            {:title "Two one armed pull ups"
                                             :holds [1, 5]
                                             :duration 0
                                             :rest 120
                                             :repeat 4
                                             }              
                                            {:title "Front levers"
                                             :holds [1]
                                             :duration 8
                                             :rest 120
                                             :repeat 3
                                             }              
                                            ]}}))

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

(defmethod event-handlers :dec-remaining [event]
  (om/transact! (root-cursor) [:current-exercise :remaining] dec))

(defmethod event-handlers :start-exercise [event]
  (om/transact! (root-cursor) :workout clojure.core/rest))

(defmethod event-handlers :update-current [event]
  (.log js/console (pr-str event))
  (om/transact! (root-cursor) (fn [s]
                                (assoc s (merge s event)
                                       :remaining (dec (:remaining s))))))

(defn handle-events [chan workouts]
  (go-loop []
    (event-handlers (<! chan))
    (recur)))

(defn main []
  (let [config (:config @app-state)]
    (swap! app-state update-in [:workouts :moon] workout/add-id )
    (om/root components/app app-state {:target (by-id "app")
                                       :shared {:config config}})
    (handle-events (:flux config) (:workouts @app-state)))
  
  #_(let [h (History.)]
      (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
      (doto h (.setEnabled true)
            (.setToken "/"))))

(defn on-reload []
  (close! workout/wall-clock))

