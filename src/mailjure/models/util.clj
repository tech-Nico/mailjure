(ns mailjure.models.util
  (:require [mailjure.backend.db :as db]))

(defn clean-entity
  "Return a new entity containing only the keys specified in the configuration of this entity. This is because
otherwise we are going to generate a query containing fields not present on the database."
[entity-name entity]
  (let [conf (get (db/get-entity-conf entity-name) :fields)]
    ;Given the conf I need to return an entity which contains only the keys of 'entity'
    ;present in the configuration
    (reduce (fn [coll [k v]]
              (assoc coll k (get entity (keyword k)))) {} conf)))
