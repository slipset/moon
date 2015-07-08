(ns cljs.moon.workout
    (:require-macros [cljs.core.async.macros :refer [go go-loop]])
    (:require [cljs.core.async :refer
               [<! >!  timeout onto-chan chan put! take! alts! close!]]))
(def workouts {
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
                                            ]})
(defn expand [{:keys [repeat] :as m}]
  (map-indexed  (fn [i coll] (assoc coll :count (inc i))) (take repeat (cycle [m]))))

(defn exercise-duration [{:keys [rest repeat duration]}]
  (* (+ rest duration) repeat))

(defn total-duration [workout]
  (reduce + (map exercise-duration workout)))

(defn done? [remaining]
  (let [state {:remaining remaining}]
    (assoc state :state 
           (cond (= remaining 30) :almost
                 (= remaining 20) :almost
                 (= remaining 10) :almost
                 (and (< remaining 4) (> remaining 0)) :almost
                 (= remaining 0) :done))))

(defn wall-clock []
  (let [output (chan)]
    (go-loop []
      (<! (timeout 1000))
      (when (>! output :tick)
        (recur)))
    output))

(defn update-current [{:keys [duration rest] :as exercise} seconds]
  (merge exercise
         (if (<= seconds duration)
           {:activity :hang :remaining (- duration seconds)
            :progress (/ seconds duration) }
           {:activity :rest :remaining (- (+ rest duration) seconds)
            :progress (/ seconds (+ rest duration))})))

(defn add-id [workout]
  (map-indexed (fn [i coll] (assoc coll :id i)) workout))

(defn prepare [workout]
  (mapcat expand (add-id workout)))

(defn create-update-current-event [current-exercise]
  {:event :update-current
         :current-exercise current-exercise})

(defn count-down [flux {:keys [duration rest clock-chan title] :as exercise}]
  (let [total (+ duration rest)]
    (when (= (:count exercise) 1)
      (put! flux {:event :start-exercise}))
    (go-loop [i 0]
      (<! clock-chan)
      (let [current-exercise (update-current exercise i)]
        (>! flux (assoc (done? (:remaining current-exercise)) :event :play))
        (>! flux (create-update-current-event current-exercise)) 
        (when (< i total)
          (recur (inc i)))))))

(defn progressor [flux clock-chan states-ch]
  (go-loop []
    (let [current-state (<! states-ch)]
      (when current-state
        (<! (count-down flux (assoc current-state :clock-chan clock-chan)))
        (recur)))))

(defn pre-workout-countdown [flux clock-channel]
  (go-loop [i 10]
    (<! clock-channel)
    (>! flux (assoc (done? i) :event :play))
    (when (> i 0)
      (>! flux {:event :dec-remaining})
      (recur (dec i)))))

(defn run [flux workout]
  (let [state-channel (chan)
        completed-channel (chan)
        clock-channel (wall-clock)
        prepared-workout (prepare workout)]
    (go (<! (pre-workout-countdown flux clock-channel))
        (progressor flux clock-channel state-channel)
        (onto-chan state-channel prepared-workout)))) 

