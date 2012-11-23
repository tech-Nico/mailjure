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
  "with-conf must surround a Korma select form. This macro will assoc the select result map with
a new map containg the entity configuration. This is to allow the presentation layer to
take the necessary actions when rendering the data. In a future revision, this macro could
check whether the entity definition has already been cached before hitting
the database.
In order for the configuration map to be merged with the result, the 'options' map must contain
a key :include-conf set to anything but false.
Refactoing so that the macro add a new meta containing the configuration so that the select
result doesn't mix with the query results"
  `(let [has-conf# (get ~options :include-conf false)
         conf# (if has-conf#
                 (->  (k/select "mljentities"
                                (k/fields :configuration)
                                (k/where {:alias (to-entity-name ~entity-name)}))
                      first
                      :configuration
                      (ch/parse-string true)))]

     (with-meta {:query ~@body}
       {:configuration (if has-conf# conf#)})))



(defmacro select-by [entity-name query-map & [options body]]
  `(with-conf ~options ~entity-name
     (k/select (resolve-table ~entity-name)
               (k/where ~query-map)
               ~@body)))


(defmacro select-all [entity-name options & body]
  `(with-conf ~options ~entity-name
    (k/select  (resolve-table ~entity-name)
               ~@body)))


(defmacro update-by-id [entity field-map & body]
  "Generic abstraction for updating an entity by specifying its PK."
  (if-let [id# (:id ~@field-map)]
    `(k/sql-only (k/update ~entity
               (k/set-fields (dissoc :id ~field-map))
               (k/set-fields {:last-modified-date (Date.) :modified-by (:id (get-current-user)) })
               (k/where { :id id#} )
               ~@body
               ))
      (throw IllegalArgumentException "field-map must contain the primary ID")))

(defn get-field-conf [entity-name field-name]
  "Given an entity-name and a field-name, returns the configuration of that field
  stored into the mljentities table"
  (let [conf (->  (k/select "mljentities"
                                (k/fields :configuration)
                                (k/where {:alias (to-entity-name entity-name)}))
                      first
                      :configuration
                      (ch/parse-string true))]
    (-> conf
        (get :fields)
        (get (keyword field-name)))))
