(ns mailjure.views.welcome
  (:require [mailjure.views.common :as common])
  (:use [noir.core :only [defpage]]
        [net.cgrand.enlive-html]
        [hiccup.core]))

(defpage "/" []
  (common/layout :enlive (html-resource "mailjure/views/welcome.html") ))

;(defpage "/" []
;  (common/layout->hiccup
;   [:h1 "HeyYYYYYYYYY.. this is the H1"]
;   [:p "And this is the P"] ))
