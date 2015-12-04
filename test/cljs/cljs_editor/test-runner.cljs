(ns cljs_editor.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [cljs_editor.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'cljs_editor.core-test))
    0
    1))
