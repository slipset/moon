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

(defn play [path] 
  (.play (new js/Audio path)))

(defn ping []
  (play "ping.mp3"))

(defn beep []
  (play "beep.mp3"))
