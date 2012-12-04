(ns mailjure.backend.db
  (:require  [clojure.string :as s]
             [korma.core :as k]
             [clj-time.core :as t]
             [clj-time.format :as tf])

  (:import  [java.sql Timestamp]))

(defonce entities-config (atom {}) )

(defn to-entity-name
"Convert a 'logical' table name into a table name as store onto the database"
  [entity-name]
  (if (<= (.lastIndexOf entity-name "Table") 0)
    (str entity-name "Table")
    entity-name))

(defn- resolve-entity-name [entity-name]
  (eval (symbol (to-entity-name entity-name))))


(defn resolve-table
"Given the entity name, returns the Var containing the table definition"
  [entity-name]
  (->> entity-name
       to-entity-name
       (symbol "mailjure.backend.core")
       eval
       ))


(defn extract-table-name [body]
  (-> (nth body 1)
      var-get)
  :name)

(defn get-entity-conf [entity-name]
  ((keyword (to-entity-name entity-name)) @entities-config))

(defn get-field-conf [entity-name field-name]
  "Given an entity-name and a field-name, returns the configuration of that field
  stored into the mljentities table"
  (-> (get-entity-conf entity-name)
      (get :fields)
      (get (keyword field-name))))


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
                 (get-entity-conf ~entity-name))]
     (with-meta  ~@body
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

(defmulti convert (fn [entity-name field-name value]
                    (let [conf (get-field-conf entity-name field-name)]
                      (println "CONVERTING  " field-name " with value " value " AND ENTITY " entity-name)
                      (println "conf :type is [[[" conf "]]]")
                       (keyword (.toLowerCase (conf :type))))))

(defmethod convert :integer [entity-name field-name value]
  (if (and ( not (nil? value)) (not (empty? (seq (str value)))))
    (Integer. value)))

(defmethod convert :decimal [entity-name field-name value]
  (Double. value))

(defmethod convert :datetime [entity-name field-name value]
  (if (not (nil? value))
    (if (= (class value) java.sql.Timestamp)
      value
      (if (and (= (class value) java.lang.String) (not (s/blank?  value)))
        (Timestamp. value)))))

(defmethod convert :default [entity-name field-name value]
  (str value))


(defn before-storing
  "This function will be called before storing an entity into the database. It's required in order to convert
data coming from an HTTP Post/Get which consider everything to be a string, into its actual format on the DB."
  [entity-name entity]
  (reduce (fn [coll [k v]]
              (assoc coll k (convert entity-name k v))) {} entity))


(defn save-entity [entity-name entity]
  (if-let [id (:id entity)]
    (k/update (resolve-table entity-name)
                (k/set-fields (merge entity {:last_modified_date (Timestamp. (System/currentTimeMillis)) :modified_by 1}))
                (k/where {:id (Integer. id)}))
    (k/insert (resolve-table entity-name)
              (k/values (merge entity {:last-modified-date (Timestamp. (System/currentTimeMillis))
                                       :creation_date (Timestamp. (System/currentTimeMillis))
                                       :created_by 1
                                       :modified-by 1})))))
