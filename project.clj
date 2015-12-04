(defproject cljs-editor "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljc"]

  :test-paths ["test/clj"]

  :dependencies [; for backend
                 [org.clojure/clojure "1.7.0"]
                 [environ "1.0.1"]
                 [compojure "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring-middleware-format "0.7.0"]
                 [enlive "1.1.6"]
                 
                 ; for frontend
                 [org.clojure/clojurescript "1.7.170" :scope "provided"]
                 [reagent "0.5.1"]
                 [re-frame "0.5.0"]
                 [secretary "1.2.3"]
                 [cljs-ajax "0.5.1"]
                ]
  
  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-environ "1.0.0"]]

  :min-lein-version "2.5.0"

  :uberjar-name "cljs-editor.jar"

  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                             :compiler {:output-to "resources/public/js/app.js"
                                        :output-dir "resources/public/js/out"
                                        :source-map "resources/public/js/out.js.map"
                                        :preamble ["react/react.min.js"]
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :clean-targets ^{:protect false} ["resources/public/js/out" "target"]
  
  :figwheel {:http-server-root "public"
             :server-port 3449
             :css-dirs ["resources/public/css"]
             :ring-handler cljs-editor.server/http-handler}
  
  :profiles {:dev {:source-paths ["env/dev/clj"]
                   :dependencies [[figwheel "0.5.0-2"]
                                  [figwheel-sidecar "0.5.0-2"]
                                  [com.cemerick/piggieback "0.1.5"]
                                  [weasel "0.6.0"]]
                   :repl-options {:init-ns cljs-editor.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :plugins [[lein-figwheel "0.5.0-2" :exclusions [org.clojure/clojure org.clojure/tools.reader]]]
                   :env {:is-dev true}}
             :test {:test-paths ["test/clj"]
                    :cljsbuild {:test-commands { "test" ["phantomjs" "env/test/js/unit-test.js" "env/test/unit-test.html"] }
                               :builds {:test {:source-paths ["src/cljs" "test/cljs"]
                                               :compiler {:output-to     "resources/public/js/app_test.js"
                                                          :output-dir    "resources/public/js/test"
                                                          :source-map    "resources/public/js/test.js.map"
                                                          :preamble      ["react/react.min.js"]
                                                          :optimizations :whitespace
                                                          :pretty-print  false}}}}}
             :uberjar {:source-paths ["env/prod/clj"]
                       :hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :main cljs-editor.server
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
