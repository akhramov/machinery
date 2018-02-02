(ns machinery.core
  (:gen-class)
  (require [machinery.web.facade :as facade]))

(defn -main
  "Entry point of application. Launches Jetty server."
  [& args]
  (facade/run))
