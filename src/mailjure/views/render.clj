(ns mailjure.views.render
  (:require [hiccup.core :as h]
            [hiccup.form :as f]
            [hiccup.page :as p]
            ))

(defn get-field-errors
  "Finds any error associated to the specified field given that errors is a list of maps. Every map has exactly 1 key-entry.
The key is the field name, the entry is a list of errors. "
  [field-name errors]
     (if (nil? errors)
       nil
       (filter #(contains? % (keyword field-name)) errors)))

(defn field-div [field-name errors & html]

  (let [field-errors (get-field-errors field-name errors)
        attrs  (merge {:id (str field-name "-div") } (if-not  (empty? field-errors) {:class "error"} {}))]
     [:div.fieldElem attrs
      html
      (when-not (empty? field-errors)
        (map  (fn [errs]
                (map #(vector :small (:message %))  ((keyword field-name) errs)))  field-errors))]))

(defmulti form-field
"Render a form field. If the :hidden configuration attribute is TRUE then simply render a
input hidden HTML control, ignoring the type and the read-only attribute. Otherwise
dispatch on the type and read-only attribute. The method dispatched on the 'type' should also
validate the value and rendere the field in an error state"
   (fn [field-conf & args]
     (let [hidden        (boolean (Boolean. (get-in field-conf [:hidden]        "FALSE")))
           read-only (boolean (Boolean. (get-in field-conf [:read-only]  "FALSE")))]
       (cond hidden
                        {:hidden hidden}
             read-only
                {:read-only read-only}
             :else
                        {:type (.toUpperCase (get-in field-conf [:type] "STRING"))}))))

;An hidden field doesn't need any customization. It's just an <input type="hidden"/>
(defmethod form-field {:hidden true} [field-conf field-name field-value  errors & options]
 (h/html
    (f/hidden-field field-name field-value)))

(defmethod form-field {:read-only true} [field-conf field-name field-value  errors & options]
 (h/html
    (f/hidden-field field-name field-value)))


(defmethod form-field {:read-only true} [field-conf field-name field-value errors & options]
   (let [lbl (-> (get field-conf :label)
                     (get :en_g))]
     (h/html
      (field-div field-name errors
          (f/label {:class "fieldLabel"} field-name lbl)
          [:div.fieldValue field-value]))))

(defmethod form-field {:type "TEXT"} [field-conf field-name field-value  errors & options]
  (let [lbl (-> (get field-conf :label)
                (get :en_gb))]

    (h/html
     (field-div field-name errors
         (f/label {:class "fieldLabel"} field-name lbl)
         (f/text-area {:class "fieldValue"
                       :cols (get field-conf :cols 100)
                       :rows (get field-conf :rows 5)} field-name field-value))


  )))

(defmethod form-field {:type "STRING"} [field-conf field-name field-value  errors & options]
  (let [lbl (-> (get field-conf :label)
                (get :en_gb))]
    (h/html
     (field-div field-name errors
        (f/label {:class "fieldLabel"} field-name lbl)
        (f/text-field {:class "fieldValue"} field-name field-value)))))



(defmethod form-field {:type "INTEGER"} [field-conf field-name field-value errors & options]
   (let [lbl (-> (get field-conf :label)
                 (get :en_g))]
     (h/html
      (field-div field-name errors
          (f/label {:class "fieldLabel"} field-name lbl)
          (f/text-field  {:class "fieldValue"} field-name field-value)))))

(defmethod form-field {:type "EMAIL"} [field-conf field-name field-value errors & options]
  (let [lbl (-> (get field-conf :label)
                (get :en_gb))]
    (h/html
     (field-div field-name errors
         (f/label {:class "fieldLabel"} field-name lbl)
         (f/email-field  {:class "fieldValue"} field-name field-value)))))

(defmethod form-field {:type "DATE"} [field-conf field-name field-value errors & options]
  (let [lbl (-> (get field-conf :label)
                (get :en_gb))]
    (h/html
     (field-div field-name errors
          (f/label {:class "fieldLabel"} field-name lbl)
          (f/text-field  {:type "date" :class "fieldValue"} field-name field-value)))))


(defn sort-entity [conf entity]
  "Given an entity record, sort the map using the 'order' configuration property found
  in the configuration json string"
  (into (sorted-map-by (fn [key1 key2]
                         (let [k1 (if (nil? key1) -1
                                      (try
                                        (Integer. (get-in conf [key1 :order]))
                                        (catch Exception ex 0)))
                               k2 (if (nil? key2) -1
                                      (try
                                        (Integer. (get-in conf [key2 :order]))
                                        (catch Exception ex 0)))]
                           (compare [k1 key1]
                                    [k2 key2])))) entity)

)
