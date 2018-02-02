(ns machinery.repository
  (:refer-clojure :exclude [sort find count])
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [monger.query :refer :all]
            [clojure.string :refer [lower-case]]
            [environ.core :refer [env]])
  (:import org.bson.types.ObjectId
           java.util.regex.Pattern))

(def ^:private collection "posts")
(def ^:pricate db-name "posts")

(def ^:private db
  (delay
   (-> (mg/connect)
       (mg/get-db db-name))))

(def ^:private per-page (delay
                         (-> (env :tg-results-per-page)
                             (Integer.))))

(defn- normalized-post [post]
  (conj post
        {:normalized (lower-case (:title post))
         :type :article}))

(defn- upsert-post [post]
  (mc/update @db collection
             {:id (:id post)}
             {$set (normalized-post post)}
             {:upsert true}))

(defn- regex-criteria [text]
  (as-> (if (nil? text) "" text) safe-text
      {:title {$regex (Pattern/quote safe-text) $options "i"}}))

(def ^:private unposted-criteria {:posted nil})

(defn- find-all-single-page [criteria]
  (with-collection @db collection
    (find criteria)
    (sort {:normalized 1})))

(defn mark-as-posted [id]
  (mc/update @db collection {:id id} {$set {:posted true}}))

(defn unposted []
  (find-all-single-page unposted-criteria))

(defn save [posts]
  (doseq [post posts] (upsert-post post)))

(defn count
  ([]
   (mc/count @db collection {}))
  ([criteria]
   (mc/count @db collection criteria)))

(defn count-by-regex [text]
  (-> text
    (regex-criteria)
    (count)))

(defn find-all [offset & {:keys [criteria] :or {:criteria {}}}]
  (->> (with-collection @db collection
        (find criteria)
        (sort {:normalized 1})
        (paginate :page (+ 1 (/ offset @per-page)) :per-page @per-page))
      (map #(dissoc % :_id))))

(defn find-by-id [id]
  (mc/find-one-as-map @db collection {:id id}))

(defn find-by-regex [offset text]
  (find-all offset :criteria (regex-criteria text)))
