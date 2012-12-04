(ns mailjure.models.validate
  (:require [mailjure.backend.db        :as db]
            [clojure.string             :as s]))

(defmulti is-valid-type? (fn [the-type the-value & _]
                         (if (nil? the-value)
                           :default
                          the-type)))

(defmethod is-valid-type? "INTEGER" [the-type field-value]
  (if-not (or (nil? field-value) (empty? (seq (str field-value))))
    (try
      (do
        (Integer. field-value)
        true)
      (catch Exception e false))
    true))

(defmethod is-valid-type? "DECIMAL" [the-type field-value]
  (try
      (do
        (Double. field-value)
        ;In my opinion an integer is a valid decimal (i.e 10.0 should be equal to 10)
        true)
      (catch Exception e (try
                          (do
                                (Integer. field-value)
                            true)
                          (catch Exception ex false)))))

(defmethod is-valid-type? "STRING" [the-type field-value]
  true)


(defmethod is-valid-type? "EMAIL" [the-type field-value]
  (let [email-pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (not (nil? (re-find email-pattern field-value)))
    )
  )

(defmethod is-valid-type? "DATETIME" [the-field field-value]

  )


(defmethod is-valid-type? :default [the-type field-value]
  true)


(defmulti valid? (fn [field-name field-value field-conf-prop]
                   (key field-conf-prop)))

(defmethod valid? :nullable [field-name field-value [k v]]
  (let [is-nullable (= (.toUpperCase v) "TRUE")]
    (when (and (not is-nullable) (s/blank? (str field-value)))
        {:error :nullable :field field-name :message (str field-name " cannot be empty")}
    )))

(defmethod valid? :type [field-name field-value [k v]]
  (let [the-type (.toUpperCase v)]
    (when-not (is-valid-type? the-type field-value)
        {:error :type :field field-name :message (str field-name " must be " the-type ". You provided " field-value)}
    )))

(defmethod valid? :default [field-name field-value [k v]]
  ;simply ignore any property which is not handled by this multimethod
  nil)



(defmacro if-not-valid
  "Validate a field by checking the configuration of the field in the mljentities table.
  On error executes the body otherwise it doesn't do anything"
        ([entity-name field-name field-value when-not-valid]
                `(if-not-valid ~field-name ~field-value ~when-not-valid nil))

        ([entity-name field-name field-value when-not-valid when-valid]
          `(let [field-conf# (db/get-field-conf ~entity-name ~field-name)
                 validation-map# (map #(valid? ~field-name ~field-value %) field-conf#)
                 not-valid# (filter #(not (nil? %)) validation-map#)]
             (if (> (count not-valid#) 0)
               (let [~'error (first not-valid#)]
                 ~when-not-valid)
               ~when-valid))))

(defn validate-field
  "Determines whether the given field-value is a valid value for the field-name specified of the
  specified entity. If any error occurs, return a sequence of errors for the specified field.
'({:error :type :field the-field ...} "
  [entity-name field-name field-value]
  (let [field-conf     (db/get-field-conf entity-name field-name)
        validation-map (map #(valid? field-name field-value %) field-conf)
        not-valid      (filter #(not (nil? %)) validation-map)]
    (when (> (count not-valid) 0)
      {(keyword field-name) not-valid})))


(defn validate-entity
"Validate an entity as per its configuration settings. Returns an error map if any validation
  error occurs, nil otherwise."
[entity-name entity]
(let [conf ((db/get-entity-conf entity-name) :fields)
      validated  (map (fn [key-value]
                        (let [field-name  (key key-value)
                              field-value (get entity field-name)]
                          (validate-field entity-name field-name field-value))) conf)
      not-valid (filter #(not (nil? %)) validated)]
  (when (> (count not-valid) 0)
    not-valid)))
