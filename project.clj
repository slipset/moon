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
                 [secretary "1.2.3"]
                 [prismatic/om-tools "0.3.11"]
                 [racehub/om-bootstrap "0.5.0"]
                 [org.omcljs/om "0.9.0"]]

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
                          :compiler {  :main moon.core
                                     :asset-path "js/compiled/out"
                                     :output-to "resources/public/js/compiled/app.js"
                                     :output-dir "resources/public/js/compiled/out"
                                     :source-map-timestamp true} } ]
              }
  ;; :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
  ;;                            :compiler {:output-to     "resources/public/js/app.js"
  ;;                                       :output-dir    "resources/public/js/out"
  ;;                                       :source-map    "resources/public/js/out.js.map"
  ;;                                       :preamble      ["react/react.min.js"]
  ;;                                       :optimizations :none
  ;;                                       :pretty-print  true}}}}

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


;; :dev {:source-paths ["env/dev/clj"]
              ;;      :test-paths ["test/clj"]

              ;;      :dependencies [[figwheel "0.2.5"]
              ;;                     [figwheel-sidecar "0.2.5"]
              ;;                     [com.cemerick/piggieback "0.1.5"]
              ;;                     [weasel "0.6.0"]]

              ;;      :repl-options {  :init-ns moon.server
              ;;                     :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

              ;;      :plugins [[lein-figwheel "0.2.5"]]

              ;;      :figwheel {:http-server-root "public"
              ;;                 :server-port 3449
              ;;                 :on-jsload moon.core/on-reload
              ;;                 :css-dirs ["resources/public/css"]}

              ;;      :env {:is-dev true}

              ;;      :cljsbuild {:test-commands { "test" ["phantomjs" "env/test/js/unit-test.js" "env/test/unit-test.html"] }
              ;;                  :builds {:app {:source-paths ["env/dev/cljs"]}
              ;;                           :test {:source-paths ["src/cljs" "test/cljs"]
              ;;                                  :compiler {:output-to     "resources/public/js/app_test.js"
              ;;                                             :output-dir    "resources/public/js/test"
              ;;                                             :source-map    "resources/public/js/test.js.map"
              ;;                                             :preamble      ["react/react.min.js"]
              ;;                                             :optimizations :whitespace
              ;;                                             :pretty-print  false}}}}}

             :uberjar {:source-paths ["env/prod/clj"]
                       :hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :whitespace
                                              :pretty-print false}}}}}})
