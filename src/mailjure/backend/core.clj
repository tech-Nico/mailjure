(ns mailjure.backend.core
 (:import (java.io File))
  (:use [monger.collection :as mc]
        [cheshire.core :as ch]
        [clojure.java.io :as io]))


(defn configure
  "Run the configuration process for a single configuration file. The configuration file must be in the form of a json string."
  [conf-file]
  (let [conf-map (parse-stream (io/reader conf-file) true)]
    conf-map
    ))

(defn init []
  (let [files (.listFiles (File. "conf"))]
    (map #(configure %1) (filter #(.endsWith ( .getName %1) ".json") files))))
