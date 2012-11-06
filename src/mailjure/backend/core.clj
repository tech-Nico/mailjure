(ns mailjure.backend.core
  (:import (java.io File)
           (java.util Date))
  (:use [korma.core])
  (:require [cheshire.core :as ch]
            [clojure.java.io :as io]))

(defonce this-ns "mailjure.backend.core")

(defn def-schema [entity db]
  "Given a map representing a mljentities row, define a new entity using korma defentity macro.
It will also use the 'configuration' column for propertly configuring the entity.
The column 'configuration' must contain a js-object-like string with proper metadata information
i.e. primary key, fields, field labels.. etc..."

  (let [table-name (keyword (:entity_name entity))
        tbl-alias  (if  (not (nil? (:alias entity))) (:alias entity) (:entity_name entity))
        conf (ch/parse-string (:configuration entity) true)
        primaryk (keyword (str (:pk conf) "ciccio"))
        default-fields (:default-fields conf)
;missing definition of the links
        ]
    (println "Initializing table " table-name)

    (intern (symbol this-ns)  (symbol  tbl-alias)
            (-> (create-entity (name tbl-alias))
                (table table-name)
                (pk  primaryk)
                (entity-fields (into [] (map #(keyword %1) default-fields)))
                (database db)))

    (println "New var " (resolve (symbol (str this-ns "/" tbl-alias))) " initialized... ")
    nil))

(defn init-entities [db]
  "init-entities read the mljentities table, and for each line it define an entity using korma defentity in order to
have the entity available for queries."
  (let [entities  (select "mljentities"
                          (fields :entity_name :alias :configuration))]
    (doseq [entity entities]
       (def-schema entity db))))

(defn init-db [db]
  (init-entities db))
