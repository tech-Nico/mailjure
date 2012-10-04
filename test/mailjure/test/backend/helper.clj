(ns mailjure.test.backend.helper
  (:require [monger.core :as m]))

(defn setup-db []
  (m/connect! {:host "localhost"})
  (m/use-db! "mailjure-test"))
