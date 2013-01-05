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
            [clojure.pprint :as pp]
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
  ;get a new ID if edit-new is true: select nextval('delivery_id_seq')
  [{:keys [entity-name id data-map errors action] :as input}]
  (let [conf        (db/get-entity-conf entity-name)
        fields-conf (:fields conf)
        data        (cond (= action "edit") (select-entity entity-name id)
                          (= action "new") 	(render/sort-entity fields-conf (u/clean-entity entity-name data-map)))
        joined (->> (merge-with (fn [conf data]
                                  (assoc  conf :data data)) fields-conf data)
                    (render/sort-entity fields-conf))]
    (common/layout :hiccup
                   (h/html
                    (p/include-js "/js/tiny_mce/tiny_mce.js")
                    (p/include-js "/js/mailjure.js")
                    (f/form-to [:post (str "/" entity-name "/" action "/" id)]
                               [:div.row
                                [:fieldset
                                 [:legend (get-in conf [:properties :name] entity-name)]
                                 [:div.five.columns
                                  (map (fn [[field-name {:keys [data]}]]
                                         (let [field (fields-conf (keyword field-name))]
                                           (render/form-field field field-name data errors))) joined)
                                  ]]
                                (f/submit-button {:class "round button"} "Save")])))))

(n/defpage get-edit "/:entity/edit/:id" {:keys [entity id]}
;Get the entity using the given ID. If no ID is specified a new entity is going to be edited.
;PROBLEMS: Need to cast ID to int. Need to distinguish between get-entity-by-id not finding the record
   (v/if-not-valid entity "id" id
        (h/html "REQUEST NOT VALID")                                     ;and ID not being an integer? Probably not
        (form {:entity-name entity :id id :data-map nil :errors nil :action "edit"})))


(n/defpage post-edit [:post "/:entity-name/edit/:id"] {:keys [entity-name id] :as entity }
  (v/if-not-valid entity-name "id" id
     (h/html "REQUEST NOT VALID")
     (let [errors (ent/save-entity entity-name entity)]
       (println "ERRORS IN Editentity: " errors)
       (if-not (empty errors)
         (resp/redirect (str "/" entity-name "/list/" id))
         (n/render form {:entity-name entity-name :id id :data-map entity :errors errors :action "edit"})))))

;Qui ho due possibilita. Generare la primary key qui o fare in modo che la pk sia generata
;dalla funzione save-entity. Se generata qui ho il problema di modificare save-entity cosi che
;gli passi un parametro per indicare che voglio INSERIRE visto che la logica attuale si basa
;sulla presenza dell'attributo ID per stabilire se deve inserire o modificare.
;se generata da save-entity ho il problema di fare in modo che l'ID non sia validato dalla
;business logic (attributo mandatory deve essere impostato a false)
(n/defpage get-new "/:entity/new/" {:keys [entity]}
 (let [new-id (ent/get-new-id entity)]          
  (form {:entity-name entity :data-map {:id new-id} :errors nil :is-new true :action "new"})))


(n/defpage post-new [:post "/:entity-name/new/"] {:keys [entity-name] :as entity }
  (let [errors (ent/save-entity entity-name entity)]
    (if-not (empty errors)
      (resp/redirect (str "/" entity-name "/list"))
      (n/render form {:entity-name entity-name :data-map entity :errors errors :action "new"}))))
