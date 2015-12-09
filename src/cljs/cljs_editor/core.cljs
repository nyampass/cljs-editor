(ns cljs-editor.core
  (:require-macros [reagent.ratom :refer [reaction]]
                   [secretary.core :refer [defroute]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as r]
            [goog.events :as events]
            [secretary.core :as secretary]
            [ajax.core :as ajax]
            [ajax.edn :as edn])
  (:import [goog History]
           [goog.history EventType]))

(def middlewares [(r/path :contents) r/trim-v])

(defn api-request [path callback & {:keys [method params] :or {method :get}}]
  (ajax/ajax-request
   (cond-> {:method method
            :uri (str "/api" path)
            :handler callback
            :response-format (edn/edn-response-format)}
     params (merge {:params params, :format (edn/edn-request-format)}))))

(r/register-handler
 :init
 (fn [_ _]
   (api-request "/filenames"
     (fn [[ok? result]]
       (r/dispatch [:update-filenames (:names result)])))
   {:filenames nil
    :filename nil
    :editing-content nil}))

(r/register-handler
 :update-filenames
 [r/trim-v]
 (fn [db [filenames]]
   (assoc db :filenames filenames)))

(r/register-handler
 :load-file
 [r/trim-v]
 (fn [db [filename]]
   (api-request (str "/files/" filename)
     (fn [[ok? result]]
       (r/dispatch [:show-content filename (:content result)])))
   db))

(r/register-handler
 :show-content
 [r/trim-v]
 (fn [db [filename content]]
   (assoc db :filename filename :editing-content content)))

(r/register-handler
 :update-content
 [(r/path :editing-content) r/trim-v]
 (fn [_ [new-content]]
   new-content))

(r/register-handler
 :save-content
 [r/trim-v]
 (fn [db [filename]]
   (api-request (str "/files/" filename)
     (fn [[ok? result]] nil)
     :method :post
     :params {:content (:editing-content db)})
   db))

(r/register-sub
 :filenames
 (fn [db _]
   (reaction (:filenames @db))))

(r/register-sub
 :editing-filename
 (fn [db _]
   (reaction (:filename @db))))

(r/register-sub
 :editing-content
 (fn [db _]
   (reaction (:editing-content @db))))

(defn menubar []
  (let [filenames (r/subscribe [:filenames])]
    (fn []
      [:ul
       (for [filename @filenames]
         ^{:key filename} [:li [:a {:href (str "#/files/" filename)} filename]])])))

(defn file-editor []
  (let [file (r/subscribe [:editing-filename])
        content (r/subscribe [:editing-content])]
    (fn []
      (letfn [(on-change [e]
                (r/dispatch [:update-content (.. e -target -value)]))
              (on-blur [e]
                (when @file
                  (r/dispatch [:save-content @file])))]
        [:div
         (when @file
           [:h1 @file])
         [:textarea {:rows 10 :value @content
                     :on-change on-change
                     :on-blur on-blur}]]))))

(defroute "/files/:filename" [filename]
  (r/dispatch [:load-file filename]))

(def history
  (doto (History.)
    (events/listen EventType.NAVIGATE
                   (fn [event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn ^:export main []
  (r/dispatch-sync [:init])
  (reagent/render [menubar] (.getElementById js/document "menubar"))
  (reagent/render [file-editor] (.getElementById js/document "file-editor")))

(set! (.-onload js/window) main)
