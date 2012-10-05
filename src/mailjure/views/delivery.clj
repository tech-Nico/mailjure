(ns mailjure.views.delivery
  (:require [mailjure.views.common :as common]
            [mailjure.models.delivery :as d]
            [noir.validation :as v]
            [monger.collection :as mc]
            [monger.db :as mdb]
            [net.cgrand.enlive-html :as en]
            )
  (:use [noir.core]
        [noir.request]
        [hiccup.element]
        [hiccup.form]
        [hiccup.page]
        [hiccup.core])
    (:import [org.bson.types ObjectId]
             [java.util Date]
             [mailjure.models.delivery Delivery]))

(defn show-error [[first-error]]
            [:p first-error ])

(defn- valid? [{:keys [ _id userdef-id from-address to-address subject html-body]}]
  (v/rule (v/has-value? userdef-id)
          [:userdef-id "The Unique Name is a mandatory field!"])
  (v/rule (v/has-value? from-address)
          [:from-address "From email address is a mandatory field!"])
  (v/rule (v/has-value? to-address)
          [:recipients "Recipient(s) not defined!"])
  (v/rule (v/has-value? subject)
          [:subject "Subject is a mandatory field!"])
  (v/rule (v/has-value? html-body)
          [:body "Content is a mandatory field!"])
  (not (v/errors? :from-address :userdef-id :to-address :subject :body)))

;TODO
(en/defsnippet edit-form "mailjure/views/edit-delivery.html" [:div#editFormContainer]
  [ {:keys [_id userdef-id from-address to-address subject body] :as delivery}]

  [:input#id] (en/set-attr :value _id)
  [:input#userdef-id] (en/set-attr :value userdef-id)
  [:input#userdef-id :> :div.error] (en/content (v/on-error :userdef-id userdef-id))
  [:input#from-address] (en/set-attr :value from-address)
  [:input#from-address :> :div.error] (en/content (v/on-error :from-address from-address))
  [:input#recipients] (en/set-attr :value to-address)
  [:input#recipients :> :div.error] (en/content (v/on-error :recipients to-address))
  [:input#subject] (en/set-attr :value subject)
  [:input#subject :> :div.error] (en/content (v/on-error :subject subject))
  [:input#elm1] (en/content body)
  [:input#elm1 :div.error] (en/content (v/on-error :body body))
  )

(defn- save-delivery
  "Save a new or existing delivery onto the database.
If the _id is specified, update the delivery otherwise create a new one"
  [{:keys [_id userdef-id from-address to-address subject body] :as delivery}]
  (d/save-delivery (d/new-delivery-from-map delivery)))

;NEW (GET)
(defpage page-new "/delivery/new" [ :as delivery]
         (common/layout :enlive
          [:h2 "Create a new delivery"]
          (edit-form delivery)))


;EDIT
(defpage page-edit "/delivery/edit/:_id" {:keys [edit-temp] :as delivery-param}
(let [delivery (if-not (nil? edit-temp)
                 delivery-param
                 ( d/get-delivery (d/new-delivery-from-map delivery-param)))]
  (common/layout :enlive

                 (edit-form delivery))))


;SEND
(defpage page-send [:post "/delivery/send"]  {:keys [_id from-address to-address subject html-body save-action] :as delivery}
  (save-delivery delivery)
  (common/layout :hiccup [:p (str  "Delivery saved and ready to be sent: " delivery)]))


;;;;;;;;;;;;;;;LIST OF DELIVERIES;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmacro edit-link [delivery label]
  `(link-to (str "/delivery/edit/" (get ~delivery :_id)) ~label))


(defpartial render-delivery [id delivery]
  (html [ :tr  (if (= id (.toString (:_id delivery))) {:class "selected-row"})
         [:td (edit-link delivery  (:_id delivery))]
         [:td (edit-link delivery  (if-let [userdef-id (:userdef-id delivery)]
                                 userdef-id
                                 "N/D"))]
         [:td (edit-link delivery  (:from-address delivery))]
         [:td (edit-link delivery  (:subject delivery))]]))


(en/defsnippet show-list "mailjure/views/delivery-list.html" [:span]
  [id]
  [:#deliveryRow] (fn [tag]
                    (html [:table
                           [:tr
                            [:th "ID"]
                            [:th "UID"]
                            [:th "FROM"]
                            [:th "SUBJECT"]
                            ]
                           (map #(render-delivery id %) (d/list-deliveries))])))


(defpage page-list "/delivery/list" []
  (common/layout :hiccup
                 (show-list nil))
)


(defpage page-list-with-id "/delivery/list/:id" {id :id}
  (common/layout :hiccup
                 (show-list id))
)

;;;;;;;;;;;;;;;;;;;;;END LIST OF DELIVERIES;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;NEW (POST)
(defpage page-new-post [:post "/delivery/new"] [:as delivery]
  (if (valid? delivery)
    (let [new-id (save-delivery delivery)]
      (render page-list-with-id  {:id  (.toString new-id)}))
    (render page-new delivery)))


 ;SAVE (POST)
(defpage page-save [:Post  "/delivery/save"] {:keys [_id] :as delivery}
(println "Call to save: " delivery)
  (if (valid? delivery)
    (do
      (let [new-id  (save-delivery delivery)]
        (println "Trying to call the list with the ID... " new-id)
        (render page-list-with-id {:id (.toString new-id)})))
    (do
      (if-not (nil? _id)
        (render page-edit (merge  delivery {:edit-temp true}))
        (render page-new delivery)))))
