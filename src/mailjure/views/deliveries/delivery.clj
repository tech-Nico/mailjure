(ns mailjure.views.deliveries.delivery
  (:require [mailjure.views.common :as common]
            [mailjure.models.delivery :as d]
            [noir.validation :as v]
            [net.cgrand.enlive-html :as en]
            )
  (:use [noir.core]
        [noir.request]
        [hiccup.page]
        [hiccup.core]
        [hiccup.element])
    (:import [org.bson.types ObjectId]
             [java.util Date]
             [mailjure.models.delivery Delivery]))


(defn- valid? [{:keys [userdef-id from-address recipients subject body]}]
  (and (v/has-value? userdef-id)
       (v/has-value? from-address)
       (v/has-value? recipients)
       (v/has-value? subject)
       (v/has-value? body)))

;TODO
(en/defsnippet edit-form "mailjure/views/deliveries/edit-delivery.html" [:div#editFormContainer]
  [title {:keys [mode _id userdef-id from-address recipients subject body] :as delivery}]
  [:form] (en/prepend {:tag :h2 :content title})
  [:input#_id]  (when-not (nil? _id) (en/set-attr :value  _id))
  [:input#userdef-id] (en/set-attr :value userdef-id)
  [:div#unique-idDiv :> :small.error] #(when (and (= mode :edit) (not (v/has-value? userdef-id))) %)
  [:input#from-address] (en/set-attr :value from-address)
  [:div#from-addressDiv :> :small.error] #(when (and (= mode :edit) (not (v/has-value? from-address))) %)
  [:input#recipients] (en/set-attr :value recipients)
  [:div#recipientsDiv :> :small.error]  #(when (and (= mode :edit) ( not (v/has-value? recipients))) %)
  [:input#subject] (en/set-attr :value subject)
  [:div#subjectDiv :> :small.error] #(when (and (= mode :edit) (not (v/has-value? subject))) %)
  [:textarea#body] (en/content body)
  [:div#bodyDiv :> :small.error] #(when (and (= mode :edit) (not (v/has-value? body))) %))


(defn- save-delivery
  "Save a new or existing delivery onto the database.
If the _id is specified, update the delivery otherwise create a new one"
  [{:keys [_id userdef-id from-address recipients subject body] :as delivery}]
  (d/save-delivery (d/new-delivery-from-map delivery)))

;NEW (GET)
(defpage page-new "/delivery/new" { :keys [mode] :as delivery}
         (common/layout :enlive
             (edit-form "New delivery" delivery)))


;EDIT
(defpage page-edit "/delivery/edit/:_id" {:keys [edit-temp] :as delivery-param}
(let [delivery (if-not (nil? edit-temp)
                 delivery-param
                 ( d/get-delivery (d/new-delivery-from-map delivery-param)))]
  (common/layout :enlive
                 (edit-form "Edit delivery" (assoc  delivery :mode :edit)))))


;SEND
(defpage page-send [:post "/delivery/send"]  {:keys [_id from-address recipients subject html-body save-action] :as delivery}
  (save-delivery delivery)
  (common/layout :hiccup [:p (str  "Delivery saved and ready to be sent: " delivery)]))


;;;;;;;;;;;;;;;LIST OF DELIVERIES;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmacro edit-link [delivery label]
  `(link-to (str "/delivery/edit/" (get ~delivery :_id)) ~label))


(defpartial render-delivery [_id delivery]
  (html [ :tr  (if (= _id (.toString (:_id delivery))) {:class "highlight"})
         [:td (edit-link delivery  (:_id delivery))]
         [:td (edit-link delivery  (if-let [userdef-id (:userdef-id delivery)]
                                 userdef-id
                                 "N/D"))]
         [:td (edit-link delivery  (:from-address delivery))]
         [:td (edit-link delivery  (:subject delivery))]]))


(en/defsnippet show-list "mailjure/views/deliveries/delivery-list.html" [:span]
  [_id]
  [:#deliveryRow] (fn [tag]
                    (html [:div.row.display.centered
                           [:table
                            [:thead
                             [:th "ID"]
                             [:th "UID"]
                             [:th "FROM"]
                             [:th "SUBJECT"]]
                            [:tbody
                             (map #(render-delivery _id %) (d/list-deliveries))]]])))


(defpage page-list "/delivery/list" []
  (common/layout :hiccup
                 (show-list nil))
)


(defpage page-list-with-id "/delivery/list/:_id" {_id :_id}
  (common/layout :hiccup
                 (show-list _id))
)

;;;;;;;;;;;;;;;;;;;;;END LIST OF DELIVERIES;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;; NEW DELIVERY ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;NEW (POST)
(defpage page-new-post [:post "/delivery/new"] [:as delivery]
  (if (valid? delivery)
    (let [new-id (save-delivery delivery)]
      (render page-list-with-id  {:_id  (.toString new-id)}))
    (render page-new delivery)))


 ;SAVE (POST)
(defpage page-save [:post  "/delivery/save"] {:keys [_id] :as delivery}
  (println "Call to save: " delivery)
  (if (valid? delivery)
    (do
      (let [new-id  (save-delivery delivery)]
        (render page-list-with-id {:_id (.toString new-id)})))
    (do
      (if-not (nil? _id)
        (render page-edit (merge  delivery {:edit-temp true}))
        (render page-new  (merge delivery {:mode :edit}))))))
