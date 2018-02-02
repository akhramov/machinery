(defproject machinery "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [morse "0.3.2"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [environ "1.1.0"]
                 [ring-logger "0.7.7"]
                 [ring/ring-json "0.5.0-beta1"]
                 [morse "0.3.3"]
                 [com.vk.api/sdk "0.5.9"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [org.apache.commons/commons-text "1.2"]
                 [org.slf4j/slf4j-log4j12 "1.7.21"]
                 [com.novemberain/monger "3.1.0"]]
  :main ^:skip-aot machinery.core
  :plugins [[lein-environ "1.1.0"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
