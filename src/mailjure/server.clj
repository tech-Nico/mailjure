(ns mailjure.server
  (:require [mailjure.backend.core :as mbc]
            [ring.adapter.jetty :as jetty]
            [mailjure.handler :as mh])
  (:use [korma.db]))

(defdb prod (postgres {:db "mailjure"
                       :user "mailjure"
                       :password "mailjure"
                       :delimiters ""}))


(defn create-handler []
  (fn [request]
    (mh/mailjure-routes)))

(def handler (create-handler))

(comment (defn -main [& m]
           (mbc/init-db prod)
           (let [port (Integer. (get (System/getenv) "PORT" "8080"))]
             (jetty/run-jetty port))))
