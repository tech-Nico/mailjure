(ns mailjure.views.deliveries.delivery
  (:require [mailjure.views.common :as common]
            [mailjure.models.entity :as d]
            [noir.validation :as v]
            [net.cgrand.enlive-html :as en]
            )
  (:use [noir.core]
        [noir.request]
        [hiccup.page]
        [hiccup.core]
        [hiccup.element]))


;;;;;;;;;;;;;;;LIST OF DELIVERIES;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmacro edit-link [entity-name entity label]
  `(link-to (str "/" ~entity-name "/edit/" (get ~entity :id)) ~label))


(defpartial render-entity [entity-name id entity]
  (html [ :tr  (if (= id (.toString (:id entity))) {:class "highlight"})
         (map #(vector :td (edit-link entity-name entity (get (key %1) entity))) entity)]))


(defn show-list [entity-name id]
  (html [:div.row.display.centered
         [:table
          [:thead
           (map #(vector :th (key %1)) (d/get-entity-field-names entity-name))
          ]
          [:tbody
           (map #(render-entity id %) (d/list-entities entity-name))]]]))

(defpage page-list "/:entity-name/list" {:keys [entity-name]}
  (common/layout :hiccup
                 (show-list entity-name nil))
)


(defpage page-list-with-id "/:entity-name/list/:id" {:keys [entity-name id]}
  (common/layout :hiccup
                 (show-list entity-name id))
)

;;;;;;;;;;;;;;;;;;;;;END LIST OF DELIVERIES;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
