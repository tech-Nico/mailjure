(ns mailjure.test.backend.db_test
  (:require [mailjure.test.backend.helper :as h]
            [mailjure.backend.db :as db]
            [mailjure.models.delivery]
            )
  (:use [clojure.test])
  (:import [mailjure.models.delivery DeliveryContent Delivery]
           [org.bson.types ObjectId]))

(h/setup-db)

(def test-content (DeliveryContent. "The HTML content for the test-create-new-entity test case" "The TEXT content"))

(def rand-id (str "myuserdef-id" (rand-int 1000)))

(def test-delivery-1 (Delivery. nil rand-id "nicobalestra@gmail.com" "test@mailjure.com" "the subject" test-content))

(def test-delivery-1-id ( db/save-entity-by-id "delivery" test-delivery-1))

(println "Testing creation of a new entity [delivery]" )

(deftest test-create-new-entity
  (is (= test-delivery-1-id (:_id (db/get-entity-by-id "delivery" test-delivery-1-id)))))

(deftest test-get-entity
  (is (not (nil? (db/get-entity-by "delivery" {:userdef-id rand-id})))
      (str "Searching a \"delivery\" by \"userdef-id\" = " rand-id " didn't find any document."))
  (println "db_test.clj - get-entity-by returns: " (db/get-entity-by "delivery" {:userdef-id rand-id}))
  (is (= test-delivery-1-id (:_id (db/get-entity-by-id "delivery" test-delivery-1-id)))
      (str "Searching a \"delivery\" with id = " test-delivery-1-id " didn't find any document.")) )
