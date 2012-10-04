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

(defpartial delivery-form [^Delivery {:keys [_id userdef-id from-address recipients subject html-body] :as delivery}]
  (if-not (nil? _id)
    (hidden-field "_id" _id))
  (hidden-field "save-action" "false")
  (include-js "/js/tiny_mce/tiny_mce.js")
(javascript-tag "tinyMCE.init({
                // General options
                mode : \"textareas\",
                theme : \"advanced\",
                plugins : \"autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,advlist,autosave,visualblocks\",

                // Theme options
                theme_advanced_buttons1 : \"save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,styleselect,formatselect,fontselect,fontsizeselect\",
                theme_advanced_buttons2 : \"cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor\",
                theme_advanced_buttons3 : \"tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen\",
                theme_advanced_buttons4 : \"insertlayer,moveforward,movebackward,absolute,|,styleprops,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,pagebreak,restoredraft,visualblocks\",
                theme_advanced_toolbar_location : \"top\",
                theme_advanced_toolbar_align : \"left\",
                theme_advanced_statusbar_location : \"bottom\",
                theme_advanced_resizing : true,

                // Example content CSS (should be your site CSS)
                content_css : \"css/content.css\",

                // Drop lists for link/image/media/template dialogs
                template_external_list_url : \"lists/template_list.js\",
                external_link_list_url : \"lists/link_list.js\",
                external_image_list_url : \"lists/image_list.js\",
                media_external_list_url : \"lists/media_list.js\",

                // Style formats
                style_formats : [
                        {title : 'Bold text', inline : 'b'},
                        {title : 'Red text', inline : 'span', styles : {color : '#ff0000'}},
                        {title : 'Red header', block : 'h1', styles : {color : '#ff0000'}},
                        {title : 'Example 1', inline : 'span', classes : 'example1'},
                        {title : 'Example 2', inline : 'span', classes : 'example2'},
                        {title : 'Table styles'},
                        {title : 'Table row 1', selector : 'tr', classes : 'tablerow1'}
                ],

                // Replace values for the template plugin
                template_replace_values : {
                        username : \"Some User\",
                        staffid : \"991234\"
                }
        });")
  [:div.three_third.clear
   [:div.one_third
    (label "userdef-id" "Unique Name:")]
   [:div.one_third
    (text-field "userdef-id" userdef-id)]
   [:div.one_third
    (v/on-error :userdef-id show-error)]]

  [:div.three_third.clear
   [:div.one_third
    (label "from-address" "From:")]
   [:div.one_third
    (text-field "from-address" from-address)]
   [:div.one_third
    (v/on-error :from-address show-error)]]

  [:div.three_third.clear
   [:div.one_third
    (label "recipients" "To:")]
   [:div.one_third
    (text-field "to-address" recipients)]
   [:div.one_third
    (v/on-error :recipients show-error)]]

  [:div.three_third.clear
   [:div.one_third
    (label "subject" "Subject:")]
   [:div.one_third
    (text-field "subject" subject)]
   [:div.one_third
    (v/on-error :subject show-error)]]

  [:div.three_third.clear
   [:div.one_third
    (label "body" "Body:")]
   [:div.two_third
    (text-area {:id "elm1" :name "elm1" :cols 40 :rows 5} "html-body" html-body)]
   [:div.one_third
    (v/on-error :body show-error)]]
  )

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
(defsnippet edit-form "mailjure/views/edit-delivery.html" [:div#editForm]
  [ {:keys [_id userdef-id from-address to-address subject body] :as delivery}]

  [:input#id] (set-attr :value _id)
  [:input#userdef-id] (set-attr :value userdef-id)
  [:input#from-address] (set-attr :value from-address)
  [:input#recipients] (set-attr :value to-address)
  [:input#subject] (set-attr :value subject)
  [:input#elm1] (content body)
  )

(defn- save-delivery
  "Save a new or existing delivery onto the database.
If the _id is specified, update the delivery otherwise create a new one"
  [{:keys [_id userdef-id from-address to-address subject body] :as delivery}]
  (d/save-delivery (d/new-delivery-from-map delivery)))

;NEW (GET)
(defpage page-new "/delivery/new" [ :as delivery]
         (common/layout :hiccup
          [:h2 "Create a new delivery"]
          (form-to {:id "save-delivery-form"} [:post "/delivery/save"]
                   (delivery-form delivery)
                   (javascript-tag
                    "function sendDelivery(){
       document.getElementById(\"save-delivery-form\").setAttribute(\"action\", '/delivery/send');
       return true;
    }")
                   (submit-button {:action-name "save"} "Save")
                   (submit-button {:onclick "sendDelivery();" :action-name "send"} "Send")
                  )))


;EDIT
(defpage page-edit "/delivery/edit/:_id" {:keys [edit-temp] :as delivery-param}
(let [delivery (if-not (nil? edit-temp)
                 delivery-param
                 ( d/get-delivery (d/new-delivery-from-map delivery-param)))]
   (common/layout :hiccup
                  (html [:h2 "Edit delivery"]
                      (form-to {:id "save-delivery-form"} [:post "/delivery/save"]
                               (delivery-form delivery)
                               (javascript-tag
                                "function sendDelivery(){
       document.getElementById(\"save-delivery-form\").setAttribute(\"action\", '/delivery/send');
       return true;
     }")
                               (submit-button {:action-name "save"} "Save")
                               (submit-button {:onclick "sendDelivery();" :action-name "send"} "Send")
                               )))))


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
