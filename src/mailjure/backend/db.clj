(ns mailjure.backend.db
  (:require  [clojure.string :as s]
             [korma.core :as k]
             [cheshire.core :as ch] )

  (:import  [java.util Date]))

(defn to-entity-name [entity-name]
  (if (<= (.lastIndexOf entity-name "Table") 0)
    (str entity-name "Table")
    entity-name))

(defn resolve-entity-name [entity-name]
  (eval (symbol (to-entity-name entity-name))))


(defn resolve-table [entity-name]
        (->> entity-name
             to-entity-name
             (symbol "mailjure.backend.core")
             eval
             ))


(defn extract-table-name [body]
  (-> (nth body 1)
      var-get)
  :name)

(defmacro with-conf [options entity-name & body]
  "merge-with-conf must surround a Korma select form. This macro will assoc the select result map with
a new map containg the entity configuration. This is to allow the presentation layer to
take the necessary actions when rendering the data. In a future revision, this macro could
check whether the entity definition has already been cached before hitting
the database."
  `(let [has-conf# (get ~options :include-conf false)
         conf# (if has-conf#
                 (->  (k/select "mljentities"
                                (k/fields :configuration)
                                (k/where {:alias (to-entity-name ~entity-name)}))
                      first
                      :configuration
                      (ch/parse-string true)))]

     { :configuration (if has-conf# conf#)
       :query ~@body}))



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
  `(k/select (resolve-table ~entity)
           (k/where {:id ~id})
           ~@body
           ))


(defmacro select-by [entity query-map & body]
  `(k/select (resolve-table ~entity)
          (k/where ~@query-map)
          ~@body))


(defmacro select-all [entity-name options & body]
  `(with-conf ~options ~entity-name
    (k/select  (resolve-table ~entity-name)
               ~@body)))
