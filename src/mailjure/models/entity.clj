(ns mailjure.models.entity
  (:require  [mailjure.backend.db :as d]
             [mailjure.backend.core :as mcore]
             [mailjure.models.validate :as v]
             [mailjure.backend.db :as d]
             [mailjure.models.util :as u])
  (:use      [korma.core])
  (:import   [java.util Date]))


(def MAX-ROWS 15)


(defn valid-entity? [entity-name]
  "Determines whether the given entity name is a valid entity which can be managed by
mailjure. By managed we mean that the entity can be accessed (CRUD) by the business logic.
Given that mailjure will give access to the entit"
  (let [entity (select "mljentities"
                         (where (or (= :entity_name (d/to-entity-name entity-name))
                                    (= :alias (d/to-entity-name entity-name)))))]
    (not (empty? entity))))



(defmacro with-check-validity [entity-name & body]
          "Surrounding a query form, this macro will check that the entity we are trying to query
          is a 'managed' entity (entity listed in the mljentities table)"
  `(let [entity-var# (d/to-entity-name ~entity-name)]
    (if (and (valid-entity? entity-var#)
             (not (nil? (d/resolve-table entity-var#))))
      ~@body
     (throw (IllegalArgumentException. (str ~entity-name " is not a valid entity!" ))))))


(defn list-entities [entity-name & options]
  "Select all instances of entity-name from the database. This function will check first
that the entity-name is one of the entities that can be \"managed\" through mailjure.
Options is a map specifying:
:firstRow The first row to return.
:maxRows Number of rows to be returned.
If no options are specified list-entities will return only the first mailjure.models.entity/MAX-ROWS rows"

  (with-check-validity entity-name
    (let [query  (d/select-all entity-name  {:include-conf true})]
      query)))


(defn get-entity-field-names [entity-name]
"Given an entity name, return the list of labels "
  )



(defn get-entity [entity-name query]
;The idea is to have the query modeled as a DSL and then have it translated
;into Kormaland

  (with-check-validity entity-name
    (d/select-by entity-name query {:include-conf true})))

(defn save-entity [entity-name entity]
  (with-check-validity entity-name
    (if-let [errors (v/validate-entity entity-name entity)]
      errors
      (->> entity
           (u/clean-entity entity-name)
           (d/save-entity entity-name)
           ))))
