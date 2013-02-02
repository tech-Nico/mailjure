(ns mailjure.views.deliveries.delivery
  (:require [hiccup.def :as hd]
            [mailjure.views.common :as common])
  (:use [liberator.core :only [defresource]]
        [compojure.core :only [ANY]]
        [hiccup.page :only [html5]]))


(defn- send-delivery-form [ctx]
  (println "HERE")
  (common/layout
        (html5 "CIAOOOOOOOOOOOOOOOO")))

(defn- send-delivery-action [ctx]
  (common/layout
   (html5 "Send the delivery now")))

(defresource send-delivery
  :handle-ok (fn [_]  (send-delivery-form))
  :post!     send-delivery-action
  :available-media-types ["text/html"])
