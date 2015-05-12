(ns moon.dom
    (:require [goog.dom :as dom]
              [goog.events :as events]
              [cljs.core.async :refer
               [chan put!]]))

(defn by-id [id]
  (dom/getElement id))

(defn listen [el type]
  (let [out (chan)]
    (events/listen el type (fn [e] (put! out e)))
    out))

(defn set-html! [el content]
  (set! (. el -innerHTML) content))

(defn html [el]
  (. el -innerHTML))

(defn toggle [btn]
  (let [v (keyword (clojure.string/lower-case (html btn)))]
    (set-html! btn (if (= :start v) "Stop" "Start"))
    v))

(defn update-status [id sofar]
  (set-html! (by-id id) (str sofar))
  sofar)
