(ns moon.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [moon.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'moon.core-test))
    0
    1))
