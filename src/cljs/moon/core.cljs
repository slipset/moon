(ns moon.core
    (:require-macros [cljs.core.async.macros :refer [go go-loop]])
    (:require [moon.dom :refer [set-html! by-id listen beep ping]]
              [cljs.moon.components :as components]
              [om.core :as om]              
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [secretary.core :as secretary :refer-macros [defroute]]
              [cljs.core.async :refer
               [<! >!  timeout onto-chan chan put! take! alts! close!]])
    (:import goog.History))

(defonce app-state (atom {:workout [
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
                                     :rest 300
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
                                     :rest 300
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
                                     :rest 120
                                     :repeat 4
                                     }              
                                    {:title "Front levers"
                                     :holds [1]
                                     :duration 8
                                     :rest 120
                                     :repeat 3
                                     }              
                                    ]}))


(defn expand [{:keys [repeat] :as m}]
  (map-indexed  (fn [i coll] (assoc coll :count (inc i))) (take repeat (cycle [m]))))

(defmulti play :state)

(defmethod play :almost [_]
  (beep))

(defmethod play :done [_]
  (ping))

(defmethod play :default [s])

(defn done? [remaining]
  (let [state {:remaining remaining}]
    (cond (and (< remaining 4) (> remaining 0))
          (assoc state :state :almost)
          (= remaining 0)
          (assoc state :state :done))))

(defn wall-clock []
  (let [output (chan)]
    (go-loop []
      (<! (timeout 1000))
      (when (>! output :tick)
        (swap! app-state update-in [:total-duration] inc)
        (recur)))
    output))

(defn count-down [{:keys [duration rest clock-chan title] :as state}]
  (let [total (+ duration rest)]
    (go-loop [i 0]
      (<! clock-chan)
      (swap! app-state assoc :current-exercise
             (merge state
                    (if (<= i duration)
                      {:activity :hang :remaining (- duration i)}
                      {:activity :rest :remaining (- (+ rest duration) i)})))
      (play (done? (get-in @app-state   [:current-exercise :remaining])))
      (when (< i total)
        (recur (inc i))))))

(defn progressor [clock-chan states-ch]
  (go-loop []
    (let [current-state (<! states-ch)]
      (when current-state
        (.log js/console (pr-str current-state))
        (when (= (:count current-state) 1)
          (swap! app-state assoc :workout (rest (:workout @app-state))))
        (<! (count-down (assoc current-state :clock-chan clock-chan)))
        
        (recur)))))

(defn run [workout]
  (let [state-channel (chan)
        completed-channel (chan)
        clock-channel (wall-clock)]
    (progressor clock-channel state-channel)
    (onto-chan state-channel workout))) 

(defn add-id [workout]
  (map-indexed (fn [i coll] (assoc coll :id i)) workout))

(defn do-workout []
  (run (mapcat expand (add-id (:workout @app-state)))))

(defn start-workout []
  (swap! app-state assoc :running-workout true)
  (do-workout))

#_(defroute  "/workout" []
    (.log js/console "starting workout")
    (start-workout))

(defn show-root []
  (let [go-chan (listen (by-id "ok") "click")]
    (swap! app-state update-in [:workout] add-id )
    (go (<! go-chan) (start-workout))))

#_(defroute "/" []
    (.log js/console "showing root")
    (show-root))

(defn main []
  (show-root)
  (swap! app-state assoc :running-workout false)
  (swap! app-state assoc :total-duration 0)
  #_(let [h (History.)]
      (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
      (doto h (.setEnabled true)
            (.setToken "/"))))

(om/root components/om-show-workout app-state
         {:target (by-id "app")})

