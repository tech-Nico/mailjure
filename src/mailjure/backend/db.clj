(ns mailjure.backend.db
  (:require  [clojure.string :as s]
             [korma.core :as k])
  (:import  [java.util Date]))



(defmacro update-by-id [entity field-map & body]
  "Generic abstraction for updating an entity by specifying its PK."
  (if-let [id# (:id ~@field-map)]
    `(k/sql-only (k/update ~entity
               (k/set-fields (dissoc :id ~field-map))
               (k/set-fields {:last-modified-date (Date.) :modified-by (:id (get-current-user)) })
               (k/where { :id id#} )
               ~@body
               ))
      (throw IllegalArgumentException "field-map must contain the primary ID")
      ))


(defmacro select-by-id [entity id & body]
  `(k/select ~entity
           (k/where {:id ~id})
           ~@body
           ))


(defmacro select-by [entity query-map & body]
  `(k/select ~entity
          (k/where ~@query-map)
          ~@body))

(defn select-all [entity-name ]
  (let [entities (k/select (eval (symbol (str "mailjure.backend.core/" entity-name "Table"))) )]
    (println "Select all returned " entities)
    entities))
