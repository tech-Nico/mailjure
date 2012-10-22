(ns mailjure.backend.db
  (:require [monger.collection :as mc]
            [clojure.string :as s])

  (:import  [org.bson.types ObjectId]
            [java.util Date]))



(defn get-entity-by-id [entity-name id]
  (let [oid (if-not (= org.bson.types.ObjectId (type id))
              (ObjectId. id)
              id)]
    (mc/find-map-by-id entity-name oid)))

(defn get-entity-by [entity-name query-map]
  (if (nil? query-map)
    (mc/find-maps entity-name)
    (mc/find-maps entity-name query-map))
  )

(defn unique?
  "True if the database doesn't contain any entity with the field = entity.field "
  [entity-name field entity]
  (if (get entity field) ;if the field is part of the entity map do the check otherwise return true
    (empty? (get-entity-by entity-name {field (field entity)} ))
    true))

(defn is-unique-fields?
  "Check to see whether the entity is unique in relation to the fields specified in the unique-fields collection"
  [entity-name unique-fields entity]
  (= 0 (count (filter false? (map #(unique? entity-name %1 entity) unique-fields)))))

(defn insert-entity
  "Insert an entity into the database. If the entity map doesn't contain an _id the function will generate a new one."
  [entity-name entity & [{:keys [unique] :as opts}]]
  (let [id (:_id entity)
        oid (condp = (nil? id)
              true  (ObjectId.)
              false (ObjectId. id))]
;for each unique field defined in the meta, check if the field is unique for this entity
    (when (is-unique-fields? entity-name (vector unique) entity)
      (mc/insert entity-name (merge entity {:_id oid :creation-date (Date.) :last-modified-date (Date.)}))
      oid)))

(defn save-entity-by-id
  "Save an entity into the db. The main idea of this function is to be able to discern whether the
entity must be inserted or updated."
  [entity-name entity & [{:as opts}]]
  (println "Call to save-entity-by-id with options " opts)
  (if-let [id  (:_id entity)]
    (let [oid (ObjectId. id)]
      (mc/update-by-id entity-name oid {"$set" (dissoc (merge entity {:last-modified-date (Date.)}) :_id)})
       oid)
    (insert-entity entity-name (dissoc entity :_id) opts))
  )
