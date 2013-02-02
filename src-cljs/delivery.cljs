(ns mailjure.delivery
  (:require [fetch.remotes :as remotes])
  (:use [jayq.core :only [$ append delegate data clj->js]]))

(def document ($))
(def action-button (document :#send-delivery))

(delegate action-button :click)
