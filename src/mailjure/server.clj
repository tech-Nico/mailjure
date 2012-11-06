(ns mailjure.server
  (:require [noir.server :as server]
            [mailjure.backend.core :as mbc])
  (:use [korma.db]))

(server/load-views "src/mailjure/views")

(defdb prod (postgres {:db "mailjure"
                       :user "mailjure"
                       :password "mailjure"
                       :delimiters ""}))

(defn -main [& m]
  (mbc/init-db prod)
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'mailjure})))
