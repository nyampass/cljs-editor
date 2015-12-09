(ns cljs-editor.server
  (:require [clojure.java.io :as io]
            [cljs-editor.dev :refer [is-dev? inject-devmode-html browser-repl start-figwheel]]
            [environ.core :refer [env]]
            [compojure.core :refer [GET POST defroutes routes context]]
            [compojure.route :refer [resources]]
            [net.cgrand.enlive-html :refer [deftemplate]]
            [net.cgrand.reload :refer [auto-reload]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.response :as res]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(deftemplate page (io/resource "index.html") []
  [:body] (if is-dev? inject-devmode-html identity))

(def file-contents (atom {"foo" "foo\nfoo\nfoo"
                          "bar" "bar\nbar\nbar"
                          "baz" "baz\nbaz\nbaz"}))

(defn response [status]
  (fn f
    ([] (f {}))
    ([data]
     (res/response (merge {:status status} data)))))

(def ok (response :ok))
(def fail (response :failure))

(def api-routes
  (-> (routes
       (GET "/filenames" []
            (ok {:names (vec (keys @file-contents))}))
       (GET "/files/:filename" [filename]
            (ok {:content (get @file-contents filename)}))
       (POST "/files/:filename" [filename content]
             (swap! file-contents assoc filename content)
             (ok)))
      (wrap-restful-format :formats [:edn])))

(defroutes app-routes
  (context "/api" [] #'api-routes)
  (GET "/" [] (page))
  (resources "/")
  (resources "/react" {:root "react"}))

(def http-handler
  (if is-dev?
    (reload/wrap-reload (wrap-defaults #'app-routes api-defaults))
    (wrap-defaults app-routes api-defaults)))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (println (format "Starting web server on port %d." port))
    (run-jetty http-handler {:port port :join? false})))

(defn run-auto-reload [& [port]]
  (auto-reload *ns*)
  (start-figwheel))

(defn run [& [port]]
  (when is-dev?
    (run-auto-reload))
  (run-web-server port))

(defn -main [& [port]]
  (run port))
