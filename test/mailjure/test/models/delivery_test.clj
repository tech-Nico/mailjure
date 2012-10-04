(ns mailjure.test.models.delivery_test
  (:use [clojure.test])
  (:require [mailjure.models.delivery :as d])
  (:import  [mailjure.models.delivery DeliveryContent Delivery]))

(def rnd-userdef-id (str "myuserdefid" (rand-int 1000)))
(def content1 (DeliveryContent. "the html content for the test-delivery test case" nil))

(def delivery1 (Delivery. nil rnd-userdef-id "test-delivery-recipient" "test-delivery-from-address" "test-delivery-subject" content1))

(def delivery1-id (d/save-delivery delivery1))
(println "Delivery-test.clj - Delivery saved: " delivery1-id)

(deftest test-delivery
  (is (not (nil? delivery1-id)) "Delivery-ID null after saving...")
  (is (= delivery1-id (:_id (first (d/get-delivery delivery1)))) "Couldn't find delivery with ID returned by the save-delivery function")
  (println "Testing list-deliveries..." (d/list-deliveries))
  (is (not (nil? (d/list-deliveries))))
  )
