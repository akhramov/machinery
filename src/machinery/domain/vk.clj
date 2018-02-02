(ns machinery.domain.vk
  (:require [environ.core :refer [env]]
            [machinery.api.vk :refer [vk]]
            [clojure.core.match :refer [match]]
            [clojure.walk :refer [keywordize-keys]]))

(def ^:private owner-id
  (delay (-> (env :vk-group-id)
             (Integer.))))

(def ^:private vk-post-url-format (delay (env :vk-post-url-format)))

(defn- retrieve-wall-posts
  ([]
   (retrieve-wall-posts (Integer. 0)))
  ([offset]
   (as-> (vk wall.get {:ownerId @owner-id, :offset offset, :count 100}) res
       (res "items")
       (case (count res)
         100 (into res (retrieve-wall-posts (Integer. (+ offset 100))))
         res))))

(defn- extract-photos [post]
  (as-> post it
      (get it "attachments")
      (filter #(= "photo" (% "type")) it)
      (first it)
      (it "photo")
      (it "photo_604")))

(defn- extract-post-data [post]
  (-> (select-keys post ["id" "text"])
      (assoc :thumb_url
             (extract-photos post))
      (assoc :url
             (format @vk-post-url-format (get post "owner_id") (get post "id")))
      (clojure.set/rename-keys {"id" :id "text" :title})))

(defn extract-data []
  (->> (retrieve-wall-posts)
    (map extract-post-data)))
