(ns machinery.domain.aggregator
  (:require [environ.core :refer [env]]
            [machinery.domain.vk :as vk]
            [machinery.domain.mega :as mega]
            [clojure.core.async :refer [chan put! >! <!!] :as async])
  (:import org.apache.commons.text.similarity.JaroWinklerDistance))

(def ^:private jaro-winkler-coef
  (delay (-> (env :jaro-winkler-coef)
             (Float.))))

(def ^:private server-root
  (delay (-> (env :server-root))))

(defn- jaro-winkler [a b]
  (-> (new JaroWinklerDistance)
      (.apply a b)))

(defn- closest-element [a source]
  (last (sort-by #(jaro-winkler (% :title) (a :title)) source)))

(defn- combine [source-a source-b]
  (reduce (fn [acc a]
            (let [candidate (closest-element a source-b)]
              (case (< @jaro-winkler-coef (jaro-winkler (a :title) (candidate :title)))
                true (->> a
                          (conj candidate)
                          (conj acc))
                false (conj acc a))))
          [] source-a))

(defn- combine-async [source-a source-b]
  (->> (partition-all (/ (count source-a) 4) source-a)
       (pmap #(combine % source-b))
       (flatten)))

(defn extract-data []
  (->> [vk/extract-data mega/extract-data]
       (pmap #(%))
       (apply combine-async)
       (map #(conj % {:dl_url (str @server-root "/" (:id %))}))))
