(ns mailjure.server
  (:require [noir.server :as server]
            [monger.core :as m]
            [mailjure.backend.core :as b]))

(server/load-views "src/mailjure/views")

(defn- setup-db []
  (m/connect! {:host "localhost"})
  (m/use-db! "mailjure")
  (b/init))


(defn -main [& m]
  (setup-db)
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'mailjure})))
