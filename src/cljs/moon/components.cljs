(ns cljs.moon.components
    (:require [clojure.string :as string]
              [goog.string :as gstring]
              [goog.string.format]
              [cljs.core.async :refer
               [put!]]
              [om.core :as om]
              [moon.dom :refer [by-id listen]]
              [om-bootstrap.button :as b]
              [om-bootstrap.random :as r]
              [om-bootstrap.table :as t]
              [om-bootstrap.progress-bar :as pb]              
              [om-tools.core :refer-macros [defcomponent]]
              [om-tools.dom :as d :include-macros true]))

(defn ->minutes [seconds]
  (gstring/format "%02d:%02d" (int (/ seconds 60))  (rem seconds 60)))

(defn ->hours [seconds]
  (if (< seconds 3600)
    (->minutes seconds)
    (let [hours (int (/ seconds 3600))
          minutes (->minutes (rem seconds 3600))]
      (gstring/format "%02d:%s" hours minutes))))

(defn- event-handler [owner event]
  (fn [e]
    (put! (om/get-shared owner [:config :flux])
          event)))

(defcomponent moon-board [data owner]
  (render [_]
          (d/div {:class "row"}
                 (d/div {:class "col-xs-12"}
                        (d/img {:class "img-rounded btn-block"
                                :src "FingerboardNumbered1.jpg"})))))

(defcomponent workout-detail [{:keys [id title holds duration rest repeat]} owner]
  (render [_]
          (d/tr {:id (str "workout-" id)}
                (d/td title)
                (d/td {:style {:text-align "right"}} repeat)
                (d/td {:style {:text-align "right"}} (->minutes duration))
                (d/td {:style {:text-align "right"}} (->minutes rest))
                (d/td {:style {:text-align "right"}} (string/join "/" holds)))))

(defcomponent exercise-detail [{:keys [id count title holds duration rest repeat]} owner]
  (render [_]
          (t/table {:striped? true}
                   (d/thead
                    (d/tr
                     (d/th "Sets")
                     (d/th "Duration")
                     (d/th "Rest")
                     (d/th "Holds"))
                    (d/tbody
                     (d/tr {:id (str "workout-" id)}
                           (d/td (str count "/" repeat))
                           (d/td (->minutes duration))
                           (d/td (->minutes rest))
                           (d/td (string/join "/" holds))))))))

(defcomponent current-exercise [{:keys [id title holds duration rest repeat remaining activity count] :as data} owner]
  (render [_]
          (d/div 
           (d/div {:class "row"}
                  (d/div {:class "col-xs-12"}
                         (d/p {:class "lead"} (if-not (= count repeat)
                                                title
                                                "Prepare for the next exercise"))))
           (d/div {:class "row"}
                  (d/div {:class "col-xs-12 exercise"}
                         (d/h1 {:class "exercise btn-block"} (r/label {:class "btn-block"
                                                              :bs-style "success"}
                                        (str (string/capitalize (name activity))
                                             " " (->minutes remaining))))
                         (pb/progress-bar {:style {:height "2px"}
                                           :min 0 :max 1 :now (:progress data)})))
           (d/div {:class "row"}
                  (d/div {:class "col-xs-12"}
                         (->exercise-detail data))))))

(defcomponent total-workout [data owner]
  (render [_]
          (t/table {:striped? true}
                   (d/thead
                    (d/tr
                     (d/th {:class "col-xs-8"} "Description")
                     (d/th {:class "col-xs-1" :style {:text-align "right"}} "Sets")
                     (d/th {:class "col-xs-1" :style {:text-align "right"}} "Duration")
                     (d/th {:class "col-xs-1" :style {:text-align "right"}} "Rest")
                     (d/th {:class "col-xs-1" :style {:text-align "right"}} "Holds")))
                   (d/tbody
                    (map ->workout-detail data)))))

(defcomponent go-button [data owner]
  (render [_]
          (when-not data
            (d/div {:class "row"}
                   (d/div {:class "col-xs-12"}
                          (b/button {:bs-style "success" :block? true :id "ok"
                                     :onClick (event-handler owner {:event :start-workout})}
                                    "Go!"))))))

(defcomponent home-button [data owner]
  (render [_]
          (when-not data
            (d/div {:class "row"}
                   (d/div {:class "col-xs-12"}
                          (b/button {:block? true 
                                     :onClick (event-handler owner {:event :home})}
                                    "Home"))))))
  
(defcomponent show-workout [data owner]
  (render [_]
          (let [current (:current-exercise data)]
            (d/div (->moon-board {})
                   (when (seq current)
                     (->current-exercise current))
                   (d/div {:class "row"}
                          (d/div {:class "col-xs-12 exercise"}
                                 (d/h1  {:class "exercise btn-block"}
                                        (r/label {:class "btn-block"} (str (if (seq current)
                                                                             "Remaining"
                                                                             "Total")) " "
                                                                             (->hours (:remaining data))))
                                 (when (seq current)
                                   (pb/progress-bar {:style {:height "2px"}
                                                     :class "total-workout"
                                                     :min 0
                                                     :max 1
                                                     :now 1  #_(/ (- (:total-duration data)
                                                                (:remaining data))
                                                             (:total-duration data))}))))
                   
                   
                   (->go-button (:running-workout data))
                   (->total-workout (:workout data))
                   (->home-button (:running-workout data))))))

(defcomponent show-workouts [workouts owner]
  (render [_]
              (d/div {} "foo")
              (d/div {:class "row"}
                     (d/ul {}
                           (map #(d/li {}
                                       (d/a  {:href "#"
                                              :onClick (event-handler owner {:event :choose-workout
                                                                :workout %1})} (name %1))) (keys workouts))))))



(defcomponent app [data owner]
  (render [_]
          (if (seq (:workout data))
            (->show-workout data)
            (->show-workouts (:workouts data)))))
