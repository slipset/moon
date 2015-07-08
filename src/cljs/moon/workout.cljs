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

(defn make-updater [current-exercise]
  (fn [s]
    (assoc s :current-exercise current-exercise
           :remaining (dec (:remaining s)))))

(defn prepare [workout]
  (mapcat expand (add-id workout)))
