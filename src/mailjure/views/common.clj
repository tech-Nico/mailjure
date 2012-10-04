(ns mailjure.views.common
  (:use [noir.core :only [defpage defpartial]]
        [hiccup.core :only [html]]
        [net.cgrand.enlive-html]))

(deftemplate layout-string "mailjure/views/container.html"
  [content-strings]
  [:#container]  (html-content (apply str (reduce #(str %1 %2) content-strings)))
  )

(deftemplate layout-list "mailjure/views/container.html"
  [& content-strings]
  [:#container :> :div.pad]  (html-content  content-strings))


(defmulti layout (fn [mode & args] mode) :default :others)

(defmethod layout :hiccup [mode & content]
  (layout-string content))

(defmethod layout :enlive [mode & content]
  (println "calling enlive with apply" )
  (layout-list content))

(defmethod layout :others  [mode & content]
(println "calling the other ")
  (layout-string content))
