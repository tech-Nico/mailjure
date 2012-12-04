(ns mailjure.test.backend.db_test
  (:require [mailjure.server :as s]
            [mailjure.backend.db :as db]
            [mailjure.backend.core :as c]
            [mailjure.models.entity :as ent])
  (:import [java.sql Timestamp])
  (:use [clojure.test]))

(c/init-db s/prod)

(deftest test-conf
  (println "CONFIGURATION " @db/entities-config)
  (is (not (nil? @db/entities-config)) "The entities configuration is still empty"))

(deftest test-conf-delivery
  (println "****** DELIVERY TABLE CONFIGURATION: " (:deliveryTable @db/entities-config))
  (is (not (nil? (:deliveryTable @db/entities-config))) "Unable to find deliveryTable configuration"))

(deftest test-get-conf-delivery
  (let [conf (db/get-entity-conf "delivery")]
    (is (not (nil? conf)) "Couldn't retrieve the configuration of delivery")))

(deftest test-get=field=conf
  (let [conf (db/get-field-conf "delivery" "id")]
    (is (not (nil? conf)) "FIeld conf is nil while it should not be." )) )

(def base-entity {:id 2 :subject "the subject" :friendly_name "friendly name" :body_html "the body"})

(deftest test-db-update-entity
  (let [ent-name "delivery"
        ent {:id 2 :subject "abc" :friendly_name "the friendly name" }
        res (db/save-entity ent-name ent)]
    (is (not ( nil? res)) (str "Error while saving the entity " ent-name ". The error was " res))))

(deftest test-entity-update-entity
  (let [ent-name "delivery"
        src (ent/save-entity ent-name base-entity)
        ]
    (is (not (nil? src) ) "Error while saving the entity")))

(deftest test-entity-update-entity
  (let [ent-name "delivery"
        ent (merge base-entity {:from_email_address "the from"}  )
        src (ent/save-entity ent-name base-entity)
        ]
    (is (not (nil? src) ) "Error while saving the entity")))
