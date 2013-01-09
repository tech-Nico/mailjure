(ns mailjure.views.listentities
  (:require [mailjure.views.common :as common]
            [mailjure.models.entity :as d]
            [net.cgrand.enlive-html :as en]
            [mailjure.backend.db :as db])
  (:use [noir.core]
        [noir.request]
        [hiccup.page]
        [hiccup.core]
        [hiccup.element]))


;;;;;;;;;;;;;;;LIST OF DELIVERIES;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn edit-link [entity-name entity label]
  (link-to (str "/" entity-name "/edit/" (get entity :id)) label))


(defn get-field-label 
  "Get the label of the specified field given the field conf map. For now it's always
  extracting the en_gb label. One day this will be I18N enabled."
  [field-conf-map]
  (->>  ((val field-conf-map) :label)
        (filter #(= (key %1) :en_gb))))

(defn render-action 
  "Render the action buttons for each entity (i.e. Send delivery)"
  [[action-name conf]]
  (let [{action :action img :icon label :label} conf]
    (html [:div [:a {:href "#"} (image {:onclick action :alt (:en_gb label)} (str "/images/"img))]])))

(defpartial render-row [entity-name field-conf actions id row]
  (html [ :tr  (if (= id (.toString (:id row))) {:class "highlight"})
           (map #(vector :td  (edit-link entity-name row ((key %1) row))) field-conf)
          ;Now... I should rendere an Icon for each action allowed on this entity.
           [:td (map render-action actions) ]
          ]))

(defn show-list [entity-name id]
  (let [rows (d/list-entities entity-name)
        conf        (db/get-entity-conf entity-name)
        field-conf (->>
                    (:fields conf)
         						(filter #(>= (read-string (get (val %1)  :order)) 0))
        				 		(sort-by #(get-in (val %1) [:order])))
        actions 	  (:actions conf)]
    (html [:div.row.display.centered
           [:table
            [:thead
             (map #(vector :th (get-field-label %1)) field-conf)
             [:th "Actions"]
             ]
            [:tbody
             (map #(render-row entity-name field-conf actions id %) rows)]]])))


;;;PAGES;;;


(defpage page-list "/:entity-name/list" {:keys [entity-name]}
  (common/layout :hiccup
                 (show-list entity-name nil))
)


(defpage page-list-with-id "/:entity-name/list/:id" {:keys [entity-name id]}
  (common/layout :hiccup
                 (show-list entity-name id))
)

;;;;;;;;;;;;;;;;;;;;;END LIST OF DELIVERIES;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
