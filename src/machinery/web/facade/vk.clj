(ns machinery.web.facade.vk
  (:require [environ.core :refer [env]]
            [machinery.domain.aggregator :refer [extract-data]]
            [machinery.domain.telegram :refer [send-channel-message]]
            [machinery.repository :refer [save mark-as-posted unposted]]))

(defn- post-notify [post]
  (Thread/sleep 5000)
  (send-channel-message post)
  (mark-as-posted (:id post)))

(defn- trigger-tg-update []
  (future (doall
           (-> (extract-data)
               (save))

           (->> (unposted)
                (map post-notify)
                (doall))))
  "ok")

(defn handler [{type "type"}]
  (condp = type
    "confirmation" (env :vk-response)
    (trigger-tg-update)))
