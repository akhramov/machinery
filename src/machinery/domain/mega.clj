(ns machinery.domain.mega
  (:require [environ.core :refer [env]]
            [clojure.java.shell :refer [sh]]
            [clojure.string :refer [split split-lines trim]]))

(def ^:private cmd
  (delay (list (env :mega-executable)
               "--username"
               (env :mega-email)
               "--password"
               (env :mega-password)
               "-en"
               (env :mega-folder))))

(def ^:private mega-file-regex
  (delay
   (-> (env :mega-file-regex)
       (re-pattern))))

(defn- execute-external-cmd []
  (-> sh
      (apply @cmd)
      (:out)))

(defn- parse-data [raw-output]
  (->> (split-lines raw-output)
       (filter (fn [x] (re-matches @mega-file-regex x)))
       (map (comp #(split % #" " 2) trim))))

(defn extract-data []
  (->> (execute-external-cmd)
      parse-data
      (map #(zipmap [:download_url :title] %))))
