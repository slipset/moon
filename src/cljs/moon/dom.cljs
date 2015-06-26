(ns moon.dom
    (:require [goog.dom :as dom]
              [goog.events :as events]
              [cljs.core.async :refer
               [chan put!]]))

(defn by-id [id]
  (dom/getElement id))

(defn listen [el type]
  (let [out (chan)]
    (events/listen el type (fn [e]  (put! out {:id (aget el "id")
                                               :event e
                                               :type type})))
    out))

(defn set-html! [el content]
  (set! (. el -innerHTML) content))

(defn html [el]
  (. el -innerHTML))

(defn play [id]
  (.play (by-id id)))

(defn ping []
  (play "ping"))

(defn beep []
  (play "beep"))
