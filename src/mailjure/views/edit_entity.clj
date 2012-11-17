(ns mailjure.views.edit-entity
  (:require [noir.core :as n]
            [hiccup.core :as h]
            [hiccup.form :as f]
            [mailjure.models.entity :as ent]))


(defmulti render-field (fn [field-conf &]
                         {:type (.toUpperCase (get-in field-conf [:type]))
                          :read-only (.toUpperCase (get-in field-conf [:read-only])) }))

(defmethod render-field {:type "STRING" :read-only "FALSE"} [field-conf field-name field-value]
  (let [lbl (-> (get field-conf :label)
                (get :en_gb))]

	(h/html
     	[:div.fieldElem {:id (str field-name "-div")}
     		(conj (f/label field-name lbl) {:class "fieldLabel"})
			(conj (f/text-field field-name field-value) {:class "fieldValue"}) 
        ]

  )))

(defmethod render-field {:type "STRING" :read-only "TRUE"} [field-conf field-name field-value]
	(let [lbl (-> (get field-conf :label)
                  (get :en_g))]
      (h/html
       [:div.fieldElem.readOnly {:id (str field-name "-div")}
        	(conj (f/label field-name lbl) {:class "fieldLabel"})
        	[:div.fieldValue field-value]])))



(n/defpage edit-entity "/:entity/edit/:id" {:keys [entity id]}
;Get the entity using the given ID. If no ID is specified a new entity is going to be edited.
(println ">>>>>>>>>>>>>>>>>>> Called")
	(let [entity (ent/get-entity-by-id entity id)]
		(h/html entity)
      )

  )
