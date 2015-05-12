(ns moon.core
    (:require-macros [cljs.core.async.macros :refer [go go-loop]])
    (:require [moon.dom :refer [set-html! by-id listen]] 
              [cljs.core.async :refer
               [<! >!  timeout onto-chan chan put! take! alts! close!]]))

(def workout [
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
              ])

(defn expand [{:keys [repeat] :as m}]
  (map-indexed  (fn [i coll] (assoc coll :count (inc i))) (take repeat (cycle [m]))))

(defn set-html [[keyword val]]
  (let [formatted-val (if (vector? val)
                        (clojure.string/join ", " val)
                        val)]
    (set-html! (by-id (str keyword)) formatted-val)))
  
(defn ->minutes [seconds]
  (str (int (/ seconds 60)) ":" (rem seconds 60)))

(defn display-set [{:keys [rest duration repeat id] :as excercise}]
  (let [total (->minutes (* (+ rest duration) repeat))
        el (by-id (str "workout-" id))]
    (aset (aget el "style") "color" "red")
    (dorun (map set-html (dissoc (assoc excercise :total total) :id)))))
    
(defn create-td [parent workout key]
  (let [td (. js/document createElement "td")]
    (. parent appendChild td)
    (set-html! td (get workout key))))
  
(defn create-row [parent workout]
  (let [tr (. js/document createElement "tr")]
    (set! (. tr -id) (str "workout-"(:id workout)))
    (. parent appendChild tr)
   (dorun (map (partial create-td tr workout ) [:title :holds :duration :rest :repeat]))))

(defn to-html [workout]
  (let [table (by-id "total")]
    (dorun (map (partial create-row table) workout))))
  
(defn wall-clock []
  (let [output (chan)]
    (go-loop []
      (<! (timeout 1000))
      (when (>! output :tick)
        (recur)))
    output))

(defn count-down [{:keys [duration rest clock-chan title] :as state} dom-updater]
  (let [total (+ duration rest)]
    (go-loop [i 0]
      (<! clock-chan)
      (if (<= i duration)
        (dom-updater (dissoc (assoc state :done i) :clock-chan))
        (dom-updater (dissoc (assoc state :rested (- i duration)) :clock-chan)))
      (when (< i total)
        (recur (inc i))))))

(defn do-state [{:keys [state] :as current-state}]
  (count-down current-state display-set))

(defn progressor [clock-chan states-ch]
  (go-loop []
    (let [current-state (<! states-ch)]
      (when current-state
        (<! (do-state (assoc current-state :clock-chan clock-chan)))
        (recur)))))

(defn run [workout]
  (let [state-channel (chan)
        completed-channel (chan)
        clock-channel (wall-clock)]
    (progressor clock-channel state-channel)
    (onto-chan state-channel workout)))

(defn main []
  (let [workout (map-indexed (fn [i coll] (assoc coll :id i)) workout)]
    (to-html workout)
    (run (mapcat expand workout))))
