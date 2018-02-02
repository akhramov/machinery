(ns machinery.web.facade.repository
  (:require [environ.core :refer [env]]
            [ring.util.response :refer [not-found redirect]]
            [machinery.repository :refer [find-by-id]]))

(defn parse-uri [uri]
  (try
    (-> uri
        (subs 1)
        (read-string))
    (catch RuntimeException e
      (prn "Runtime exception occured" e)
      0)))


(defn handler [uri]
  (-> uri
      (parse-uri)
      (find-by-id)
      (:download_url)
      (#(if (nil? %) (not-found "No data yet. :(") (redirect %)))))
