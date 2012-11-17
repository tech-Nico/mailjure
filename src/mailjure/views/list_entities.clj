(ns mailjure.views.list-entities
  (:require [mailjure.views.common :as common]
            [mailjure.models.entity :as d]
            [noir.validation :as v]
            [net.cgrand.enlive-html :as en]
            [noir.response :as nr :only status]
            )
  (:use [noir.core]
        [noir.request]
        [hiccup.page]
        [hiccup.core]
        [hiccup.element]))


;;;;;;;;;;;;;;;LIST OF DELIVERIES;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn edit-link [entity-name entity label]
  (link-to (str "/" entity-name "/edit/" (get entity :id)) label))


(defn get-field-label [field-conf-map]
  (->>  ((val field-conf-map) :label)
        (filter #(= (key %1) :en_gb))))


(defpartial render-row [entity-name fields id row]
  (html [ :tr  (if (= id (.toString (:id row))) {:class "highlight"})
           (map #(vector :td  (edit-link entity-name row ((key %1) row))) fields)]))

(defn show-list [entity-name id]
  (println "TEST: " (d/list-entities entity-name))
  (let [query (d/list-entities entity-name)
        field-conf ((:configuration query) :fields)
        field-conf (filter #(>= (read-string (get (val %1)  :order)) 0) field-conf)
        field-conf (sort-by #(get-in (val %1) [:order]) field-conf)
        rows (:query query)]
    (html [:div.row.display.centered
           [:table
            [:thead
             (map #(vector :th (get-field-label %1)) field-conf)
             ]
            [:tbody
             (map #(render-row entity-name field-conf id %) rows)]]])))

(defpage page-list "/:entity-name/list" {:keys [entity-name]}
  (common/layout :hiccup
                 (show-list entity-name nil))
)


(defpage page-list-with-id "/:entity-name/list/:id" {:keys [entity-name id]}
  (common/layout :hiccup
                 (show-list entity-name id))
)

;;;;;;;;;;;;;;;;;;;;;END LIST OF DELIVERIES;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
