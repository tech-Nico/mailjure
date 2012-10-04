(ns mailjure.models.delivery
  (:require [monger.collection :as mc]
            [mailjure.backend.db :as mdb])
  (:import [org.bson.types ObjectId]
           [java.util Date]))

(defprotocol IDelivery
  "Defines the operations allowed on a delivery entity."
  (send-delivery   [delivery])
  (save-delivery   [delivery]
    "Save the specified delivery onto the database.
The operation can be either an Insert or Update depending upon the presence of the _id attribute in the delivery record"))

(defrecord DeliveryContent [body-html body-text])

(defrecord Delivery [_id userdef-id recipients from-address subject content]
  IDelivery
  (send-delivery [this])
  (save-delivery [this]
    (mdb/save-entity-by-id "delivery" this))
  )

(defmacro new-delivery-from-map [new-map]
  `(merge (Delivery. nil nil nil nil nil nil) ~new-map))

(defn get-delivery [this]
    (cond
     (not (nil? (:_id this))) (mdb/get-entity-by-id "delivery" (:_id this))
     (not (nil? (:userdef-id this))) (mdb/get-entity-by "delivery" {:userdef-id (:userdef-id this)})
     :else nil))

(defn list-deliveries []
  (mdb/get-entity-by "delivery" nil))
