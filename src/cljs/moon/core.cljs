(ns moon.core
    (:require-macros [cljs.core.async.macros :refer [go go-loop]])
    (:require [moon.dom :refer [set-html! by-id listen beep ping]]
              [clojure.string :as string]
              [goog.string :as gstring]
              [goog.string.format]
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

(defn ->minutes [seconds]
  (gstring/format "%02d:%02d" (int (/ seconds 60))  (rem seconds 60)))

(defn format [keyword val]
  (cond (vector? val) (string/join ", " val)
        (= :rest keyword) (->minutes val)
        (= :duration keyword) (->minutes val)
        :else val))

(defn set-html [[keyword val]]
  (set-html! (by-id (str keyword)) (format keyword val)))
  
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

(defn display-set [{:keys [count activity rest duration repeat id] :as excercise} remaining]
  (let [total (->minutes (* (+ rest duration) repeat))
        el (by-id (str "workout-" id))]
    (play (done? remaining))
    (.setAttribute el "class" "success")
    (set-html! (by-id ":remaining") (->minutes remaining))
    (set-html! (by-id ":type") (string/capitalize (name activity)))
    (dorun (map set-html (dissoc (assoc excercise :total total) :id :activity)))))
    
(defn create-td [parent workout key]
  (let [td (. js/document createElement "td")]
    (. parent appendChild td)
    (set-html! td (format key (get workout key)))))
  
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
        (dom-updater (dissoc (assoc state :activity :hang) :clock-chan) (- duration i))
        (dom-updater (dissoc (assoc state :activity :rest) :clock-chan) (- (+ rest duration) i)))
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

(defn add-id [workout]
  (map-indexed (fn [i coll] (assoc coll :id i)) workout))

(defn do-workout []
  (run (mapcat expand (add-id workout))))

(defn show-workout []
  (let [total (by-id "ok")
        workout (by-id "workout")]
    (aset (aget total "style") "display" "none")
    (aset (aget workout "style") "display" "block")
    (set-html! (by-id "total-header") "Remaining")
    (.scroll js/window 0)))

(defn start-workout []
  (show-workout)
  (do-workout))
        
(defn main []
  (let [go-chan (listen (by-id "ok") "click")]
    (to-html (add-id workout))
    (go (<! go-chan) (start-workout))))
             
