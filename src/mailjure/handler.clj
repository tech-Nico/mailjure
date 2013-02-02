(ns mailjure.handler
  (:use [compojure.core :only [routes ANY]])
  (:require [mailjure.views.deliveries.delivery :as delivery]))

(defn mailjure-routes []
  (routes
   (ANY "/delivery/send" [] delivery/send-delivery)))
