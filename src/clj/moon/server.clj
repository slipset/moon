(ns moon.server
    (:require [clojure.java.io :as io]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defroutes routes
  (resources "/")
  (GET "/*" req (io/resource "public/index.html")))

(def http-handler
  (wrap-defaults routes api-defaults))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (print "Starting web server on port" port ".\n")
    (run-jetty http-handler {:port port :join? false})))

(defn run [& [port]]
  (run-web-server port))

(defn -main [& [port]]
  (run port))

