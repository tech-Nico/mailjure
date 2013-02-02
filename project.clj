(defproject mailjure "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [com.draines/postal "1.8.0"]
                           [korma "0.3.0-beta9"]
                           [postgresql "9.1-901.jdbc4"]
                           [enlive "1.0.1"]
                           [cheshire "5.0.0"]
                           [metis "0.2.1"]
                           [clj-time "0.4.4"]
                           [liberator "0.8.0"]
                           [compojure "1.0.4"]
                           [ring "1.1.8"
                           ]]

                           ;; ClojureScript
                           ;[jayq "0.1.0-alpha2"]
                           ;[clj-http "0.4.3"]
                           ;[clojail "1.0.3"]
                           ;[fetch "0.1.0-alpha2"]
                           ;[jayq "0.3.2"]
                           ;[crate "0.2.2"]]
            ;:plugins [[lein-cljsbuild "0.2.9"]]
            ;:cljsbuild  {:builds
            ;             [{:builds nil,
            ;               :source-path "src-cljs",
            ;               :compiler {:pretty-print true,
            ;                          :output-to "resources/public/js/cljs.js",
            ;                          :optimizations :simple}}]}
            :ring {:handler mailjure.server/handler
                   :adapter {:port 8000}}

            :main mailjure.server)
