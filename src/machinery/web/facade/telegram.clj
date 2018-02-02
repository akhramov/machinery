(ns machinery.web.facade.telegram
  (:require [machinery.domain.telegram :refer [bot-api]]))

(defn handler [params]
  (->> params
       (clojure.walk/keywordize-keys)
       (vector)
       (map bot-api)
       (doall))
  "ok")
