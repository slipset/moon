(defproject moon "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}


  :repl-options {:timeout 200000} ;; Defaults to 30000 (30 seconds)

  :test-paths ["spec/clj"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122" ]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.3"]
                 [compojure "1.4.0"]                 
                 [secretary "1.2.3"]
                 [prismatic/om-tools "0.3.11"]
                 [racehub/om-bootstrap "0.5.0"]
                 [org.omcljs/om "0.9.0"]
                 [environ "1.0.1"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.0"]]

  :source-paths ["src/clj"]

  :min-lein-version "2.5.0"

  :uberjar-name "moon.jar"

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {
              :builds [ {:id "example"
                         :source-paths ["src/cljs"]
                         :figwheel true
                         :compiler {
                                    :main moon.core
                                    :asset-path "js/compiled/out"
                                    :output-to "resources/public/js/compiled/app.js"
                                    :output-dir "resources/public/js/compiled/out"
                                    :source-map-timestamp true} } ]
              }

  :figwheel {
             ;; Start an nREPL server into the running figwheel process
             :nrepl-port 7888
             ;; Load CIDER, refactor-nrepl and piggieback middleware
             :nrepl-middleware ["cider.nrepl/cider-middleware"
                                "refactor-nrepl.middleware/wrap-refactor"
                                "cemerick.piggieback/wrap-cljs-repl"]
             }
  :profiles { :dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                   [org.clojure/tools.nrepl "0.2.10"]]
                    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

              :uberjar {:source-paths ["env/prod/clj"]
                        :hooks [leiningen.cljsbuild]
                        :env {:production true}
                        :omit-source true
                        :aot :all
                        :cljsbuild {:jar true
                                    :main "moon.core" 
                                    :builds [{:source-paths ["src/cljs"]
                                              :compiler {:optimizations :advanced
                                                         :pretty-print false}}] }}})
