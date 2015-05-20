(ns cljs.moon.components
    (:require [clojure.string :as string]
              [goog.string :as gstring]
              [goog.string.format]
              [om.core :as om]
              [om-tools.core :refer-macros [defcomponent]]
              [om-tools.dom :as dom :include-macros true]))

(defn ->minutes [seconds]
  (gstring/format "%02d:%02d" (int (/ seconds 60))  (rem seconds 60)))

(defcomponent om-show-moon-board [data owner]
  (render [_]
          (dom/div {:class "row"}
                   (dom/div {:class "col-sm-12"}
                            (dom/img {:class "img-rounded"
                                      :src "FingerboardNumbered1.jpg"})))))

(defcomponent om-show-workout-detail [{:keys [id title holds duration rest repeat]} owner]
  (render [_]
          (dom/tr {:id (str "workout-" id)}
                  (dom/td title)
                  (dom/td {:style {:text-align "right"}} (string/join "/" holds))
                  (dom/td {:style {:text-align "right"}} (->minutes duration))
                  (dom/td {:style {:text-align "right"}} (->minutes rest))
                  (dom/td {:style {:text-align "right"}} repeat))))

(defcomponent om-show-exercise-detail [{:keys [id count title holds duration rest repeat]} owner]
  (render [_]
          (dom/table {:class "table table-striped"}
                     (dom/tbody
                      (dom/tr
                       (dom/th "Holds")
                       (dom/th "Duration")
                       (dom/th "Rest")
                       (dom/th "Sets"))
                      (dom/tr {:id (str "workout-" id)}
                              (dom/td (string/join "/" holds))
                              (dom/td (->minutes duration))
                              (dom/td (->minutes rest))
                              (dom/td (str count "/" repeat)))))))

(defcomponent om-show-current-exercise [{:keys [id title holds duration rest repeat remaining activity] :as data} owner]
  (render [_]
          (dom/div
           (dom/div {:class "row heading"}
                    (dom/div {:class "col-sm-12"}
                             (dom/h1 "Excercise")
                             (dom/p {:class "lead"} title)))
           (dom/div {:class "row"}
                    (dom/div {:class "col-sm-12"}
                             (dom/div {:class "block btn-block primary"}
                                      (dom/h3 (str (string/capitalize (name activity))
                                                   " " (->minutes remaining))))))
           (dom/div {:class "row"}
                    (dom/div {:class "col-sm-12"}
                             (->om-show-exercise-detail data))))))

(defcomponent om-show-total-workout [data owner]
  (render [_]
          (dom/table {:class "table table-striped"}
                     (dom/tbody
                      (dom/tr
                       (dom/th "Description")
                       (dom/th {:style {:text-align "right"}} "Holds")
                       (dom/th {:style {:text-align "right"}} "Duration")
                       (dom/th {:style {:text-align "right"}} "Rest")
                       (dom/th {:style {:text-align "right"}} "Sets"))
                     (map ->om-show-workout-detail data)))))

(defcomponent om-show-workout [data owner]
  (render [_]
          (let [running (:running-workout data)]
            (dom/div (->om-show-moon-board {})
                     (when running
                       (->om-show-current-exercise (:current-exercise data)))
                     (dom/div {:class "row heading"}
                              (dom/div {:class "col-sm-12"}
                                       (dom/h1 "Total workout")))
                     (when-not running
              (dom/div {:class "row"}
                       (dom/div {:class "col-sm-12"}
                                (dom/button {:class "btn block btn-block primary btn-primary"
                                             :id "ok"} "Go!"))))
                     (->om-show-total-workout (:workout data))))))

