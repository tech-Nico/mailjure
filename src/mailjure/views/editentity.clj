(ns mailjure.views.editentity
  (:require [hiccup.core :as h]
            [noir.core :as n]
            [noir.response :as resp]
            [hiccup.form :as f]
            [hiccup.page :as p]
            [mailjure.models.validate :as v]
            [mailjure.models.entity :as ent]
            [mailjure.views.common :as common]
            [mailjure.views.render :as render]
            [mailjure.backend.db :as db]
            [mailjure.models.util :as u]
            ))


(defn- select-entity [entity-name id]
  (let [query (ent/get-entity entity-name {:id (Integer. id)})
        conf  ((meta query) :configuration)
        fields-conf (conf :fields) ]
    (->> (first query)
         (filter  (fn [[k v]]
         (not (nil? (get fields-conf k)))))
         (render/sort-entity fields-conf))))

(n/defpartial form
  [{:keys [entity-name id data-map errors] :as input}]
  (let [conf        (db/get-entity-conf entity-name)
        fields-conf (:fields conf)
        data        (if (nil? data-map)
                          (select-entity entity-name id)
                          (render/sort-entity fields-conf (u/clean-entity entity-name data-map)))]
    (common/layout :hiccup
                   (h/html
                    (p/include-js "/js/tiny_mce/tiny_mce.js")
                    (p/include-js "/js/mailjure.js")
                    (f/form-to [:post (str "/" entity-name "/edit/" id)]
                               [:div.row
                                [:fieldset
                                 [:legend (get-in conf [:properties :name] entity-name)]
                                 [:div.five.columns
                                  (map (fn [[field-name value]]
                                         (let [field (fields-conf (keyword field-name))]
                                           (render/form-field field field-name value errors))) data)
                                  ]]
                                (f/submit-button {:class "round button"} "Save")])))))

(n/defpage get-edit "/:entity/edit/:id" {:keys [entity id]}
;Get the entity using the given ID. If no ID is specified a new entity is going to be edited.
;PROBLEMS: Need to cast ID to int. Need to distinguish between get-entity-by-id not finding the record
   (v/if-not-valid entity "id" id
        (h/html "REQUEST NOT VALID")                                     ;and ID not being an integer? Probably not
        (form {:entity-name entity :id id :data-map nil :errors nil})))

(n/defpage post-edit [:post "/:entity-name/edit/:id"] {:keys [entity-name id] :as entity }
  (v/if-not-valid entity-name "id" id
     (h/html "REQUEST NOT VALID")
     (let [errors (ent/save-entity entity-name entity)]
       (if (< (count errors) 0)
         (resp/redirect (str "/" entity-name "/list/" id))
         (n/render form {:entity-name entity-name :id id :data-map entity :errors errors})))))
