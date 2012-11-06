(ns mailjure.models.entity
  (:require  [mailjure.backend.db :as db]
             [mailjure.backend.core :as mcore])
  (:use [korma.core])
  (:import   [java.util Date]))


(def MAX-ROWS 15)

(defn- to-entity-name [entity-name]
  (if (<= (.lastIndexOf entity-name "Table") 0)
    (str entity-name "Table")
    entity-name))

(defn valid-entity? [entity-name]
  "Determines whether the given entity name is a valid entity which can be managed by
mailjure. By managed we mean that the entity can be accessed (CRUD) by the business logic.
Given that mailjure will give access to the entities by querying for their name in the URL,
this check is exremely important to prevent accessing unautorized entities"
  (let [entity (select "mljentities"
                       (where (or (= :entity_name (to-entity-name entity-name))
                                  (= :alias (to-entity-name entity-name)))))]
    (not (empty? entity)))
  )

(defmacro with-check-validity [entity-name & body]
  `(let [entity-var# (to-entity-name ~entity-name)]
     (println "Checking that " entity-var# " is found...")
     (println "Test1 " (valid-entity? entity-var#))
     (println "Test2 " (str "mcore/" entity-var#) "-> " (->> entity-var# name (symbol "mailjure.backend.core") resolve))
    (if (and (valid-entity? entity-var#)
             (not (nil? (resolve (symbol (str "mailjure.backend.core/" entity-var#))))))
      ~@body
     (throw (IllegalArgumentException. (str ~entity-name " is not a valid entity!" ))))))


(defn- resolve-entity-name [entity-name]
  (eval (symbol (to-entity-name entity-name))))

(defn get-entity [entity-name entity]
  (with-check-validity entity-name
    (cond
     (not (nil? (:_id entity))) (db/select-by entity-name {:id (:_id entity)})
     (not (nil? (:userdef-id entity))) (db/select-by entity-name {:userdef-id (:userdef-id entity)})
     :else nil)))

(defn list-entities [entity-name & options]
  "Select all instances of entity-name from the database. This function will check first
that the entity-name is one of the entities that can be \"managed\" through mailjure.
Options is a map specifying:
:firstRow The first row to return.
:maxRows Number of rows to be returned.
If no options are specified list-entities will return only the first mailjure.models.entity/MAX-ROWS rows"
  (with-check-validity entity-name
    (db/select-all entity-name)))


(defn get-entity-field-names [entity-name])
