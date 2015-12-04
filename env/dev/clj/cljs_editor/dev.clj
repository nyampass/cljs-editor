(ns cljs-editor.dev
  (:require [environ.core :refer [env]]
            [net.cgrand.enlive-html :refer [set-attr prepend append html]]
            [cemerick.piggieback :as piggieback]
            [weasel.repl.websocket :as weasel]
            [figwheel-sidecar.repl-api :as fig]))

(def is-dev? (env :is-dev))

(def inject-devmode-html
  (comp
     (set-attr :class "is-dev")
     (prepend (html [:script {:type "text/javascript" :src "/js/out/goog/base.js"}]))
     (append  (html [:script {:type "text/javascript"} "goog.require('cljs_editor.main')"]))))

(defn browser-repl []
  (let [env (weasel/repl-env :ip "0.0.0.0" :port 9001)]
    (piggieback/cljs-repl env)))

(defn start-figwheel []
  (fig/start-figwheel!))
