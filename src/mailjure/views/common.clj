(ns mailjure.views.common
  (:use [hiccup.core :only [html]]
        [net.cgrand.enlive-html :only [deftemplate html-content content]]))

(deftemplate layout-string "mailjure/views/container.html"
  [content-strings]
  [:#container]  (html-content (apply str (reduce #(str %1 %2) content-strings)))
  )

(deftemplate layout-list "mailjure/views/container.html"
  [& content-strings]
  [:#container :> :div.pad]  (content  content-strings))


(defmulti layout (fn [mode & args] mode) :default :others)

(defmethod layout :hiccup [mode & content]
  (layout-string content))

(defmethod layout :enlive [mode & content]
  (layout-list content))

(defmethod layout :others  [mode & content]
  (layout-string content))
