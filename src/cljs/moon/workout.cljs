(ns cljs.moon.workout
    (:require-macros [cljs.core.async.macros :refer [go go-loop]])
    (:require [cljs.core.async :refer
               [<! >!  timeout onto-chan chan put! take! alts! close!]]))

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

(defn update-current [current-exercise]
  {:event :update-current
         :current-exercise current-exercise})

(defn count-down [flux {:keys [duration rest clock-chan title] :as exercise}]
  (let [total (+ duration rest)]
    (when (= (:count exercise) 1)
      (put! flux {:event :start-exercise}))
    (go-loop [i 0]
      (<! clock-chan)
      (let [current-exercise (update-current exercise)]
        (>! flux (assoc (done? (:remaining current-exercise)) :event :play))
        (>! flux (update-current current-exercise)) 
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

