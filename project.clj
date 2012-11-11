(defproject mailjure "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [com.draines/postal "1.8.0"]
                           [korma "0.3.0-beta9"]
                           [postgresql "9.1-901.jdbc4"]
                           [enlive "1.0.1"]
                           [noir "1.3.0-beta9"]
                           [cheshire "4.0.3"]]

                           :main mailjure.server)
