(ns cljs-editor.core
  (:require-macros [reagent.ratom :refer [reaction]]
                   [secretary.core :refer [defroute]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as r]
            [goog.events :as events]
            [secretary.core :as secretary])
  (:import [goog History]
           [goog.history EventType]))

(def middlewares [(r/path :contents) r/trim-v])

(r/register-handler
 :init
 (fn [_ _]
   (let [filename "foo"
         contents {"foo" "foo\nfoo\nfoo"
                   "bar" "bar\nbar\nbar"
                   "baz" "baz\nbaz\nbaz"}]
    {:filename filename
     :editing-content (get contents filename)
     :contents contents})))

(r/register-handler
 :load-file
 [r/trim-v]
 (fn [db [filename]]
   (assoc db :filename filename
             :editing-content (get-in db [:contents filename]))))

(r/register-handler
 :update-content
 [(r/path :editing-content) r/trim-v]
 (fn [_ [new-content]]
   new-content))

(r/register-handler
 :save-content
 [r/trim-v]
 (fn [db [filename]]
   (assoc-in db [:contents filename] (:editing-content db))))

(r/register-sub
 :filenames
 (fn [db _]
   (reaction (keys (:contents @db)))))

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
         ^{:key filename} [:li [:a {:href (str "#/" filename)} filename]])])))

(defn file-editor []
  (let [file (r/subscribe [:editing-filename])
        content (r/subscribe [:editing-content])]
    (fn []
      (letfn [(on-change [e]
                (r/dispatch [:update-content (.. e -target -value)]))
              (on-blur [e]
                (r/dispatch [:save-content @file]))]
        [:div
         [:h1 @file]
         [:textarea {:rows 10 :value @content
                     :on-change on-change
                     :on-blur on-blur}]]))))

(defroute "/" []
  (r/dispatch [:load-file "foo"]))

(defroute "/:filename" [filename]
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
