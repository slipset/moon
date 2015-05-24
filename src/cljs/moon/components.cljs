(ns cljs.moon.components
    (:require [clojure.string :as string]
              [goog.string :as gstring]
              [goog.string.format]
              [om.core :as om]
              [om-bootstrap.button :as b]
              [om-bootstrap.random :as r]              
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
                (d/td {:style {:text-align "right"}} (string/join "/" holds))
                (d/td {:style {:text-align "right"}} (->minutes duration))
                (d/td {:style {:text-align "right"}} (->minutes rest))
                (d/td {:style {:text-align "right"}} repeat))))

(defcomponent exercise-detail [{:keys [id count title holds duration rest repeat]} owner]
  (render [_]
          (d/table {:class "table table-striped"}
                   (d/tbody
                    (d/tr
                     (d/th "Holds")
                     (d/th "Duration")
                     (d/th "Rest")
                     (d/th "Sets"))
                    (d/tr {:id (str "workout-" id)}
                          (d/td (string/join "/" holds))
                          (d/td (->minutes duration))
                          (d/td (->minutes rest))
                          (d/td (str count "/" repeat)))))))

(defcomponent current-exercise [{:keys [id title holds duration rest repeat remaining activity count] :as data} owner]
  (render [_]
          (d/div
           (d/div {:class "row heading"}
                  (d/div {:class "col-xs-12"}
                         (d/h1 "Exercise")
                         (d/p {:class "lead"} (if-not (= count repeat)
                                                title
                                                "Prepare for the next exercise"))))
           (d/div {:class "row"}
                  (d/div {:class "col-xs-12"}
                         (d/h1 {:class "btn-block"} (r/label {:class "btn-block"
                                                              :bs-style "success"}
                                        (str (string/capitalize (name activity))
                                           " " (->minutes remaining))))))
           (d/div {:class "row"}
                  (d/div {:class "col-xs-12"}
                         (->exercise-detail data))))))

(defcomponent total-workout [data owner]
  (render [_]
          (d/table {:class "col-xs-12 table table-striped"}
                   (d/tbody
                    (d/tr
                     (d/th {:class "col-xs-8"} "Description")
                     (d/th {:class "col-xs-1" :style {:text-align "right"}} "Holds")
                     (d/th {:class "col-xs-1" :style {:text-align "right"}} "Duration")
                     (d/th {:class "col-xs-1" :style {:text-align "right"}} "Rest")
                     (d/th {:class "col-xs-1" :style {:text-align "right"}} "Sets"))
                    (map ->workout-detail data)))))

(defcomponent go-button [data owner]
  (render [_]
          (when-not data
            (d/div {:class "row"}
                   (d/div {:class "col-xs-12"}
                          (b/button {:bs-style "success" :block? true :id "ok"}
                                    "Go!"))))))

(defcomponent show-workout [data owner]
  (render [_]
          (let [current (:current-exercise data)]
            (d/div (->moon-board {})
                   (when (seq current)
                     (->current-exercise current))
                   (d/div {:class "row heading"}
                          (d/h1 {:class "col-xs-6"} "Total")
                          (d/h1 {:class "col-xs-6"} (->hours (:total-duration data))))
                   
                   (->go-button (:running-workout data))
                   (->total-workout (:workout data))))))
