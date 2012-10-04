(ns mailjure.backend.db
  (:require [monger.collection :as mc])
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

(defn insert-entity
  "Insert an entity into the database. If the entity map doesn't contain an _id the function will generate a new one."
  [entity-name entity]
  (let [id (:_id entity)
        oid (condp = (nil? id)
              true  (ObjectId.)
              false (ObjectId. id))]
    (mc/insert entity-name (merge entity {:_id oid :creation-date (Date.) :last-modified-date (Date.)}))
    oid
    )
  )

(defn save-entity-by-id
  "Save an entity into the db. The main idea of this function is to be able to discern whether the
entity must be inserted or updated."
  [entity-name entity]
  (if-let [id (:_id entity)]
    (let [oid (ObjectId. id)]
      (mc/update-by-id entity-name oid {"$set" (dissoc (merge entity {:last-modified-date (Date.)}) :_id)})
       oid)
    (insert-entity entity-name entity))
  )
