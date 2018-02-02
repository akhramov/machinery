(ns machinery.domain.telegram
  (:require [morse.handlers :as h]
            [environ.core :refer [env]]
            [morse.api :as t]
            [machinery.repository :as repo]))

(def ^:private tg-hook-uri
  (delay (str (env :server-root) (env :tg-uri))))

(def ^:private token
  (delay
   (let [token (env :tg-token)]
     (t/set-webhook token @tg-hook-uri)
     token)))

(def ^:private per-page
  (delay (-> (env :tg-results-per-page)
             (Integer.))))

(def ^:private post-template "VK: %s\nDL: %s") ;; https://github.com/systemd/systemd/issues/3729
(def ^:private channel-id (delay (env :tg-channel-id)))

(defn- message-text [post]
  (format post-template
          (:url post)
          (:dl_url post)))

(defn send-channel-message [post]
  (t/send-text @token @channel-id (message-text post)))

(defn- input-message-content [result-item]
  {:input_message_content
   {:message_text (message-text result-item)}})

(defn- apply-additional-fields [result]
  (->> result
       (map #(conj % (input-message-content %)))))

(defn- parse-offset [offset]
  (if (clojure.string/blank? offset)
    0
    (Integer. offset)))

(defn- next-offset [offset query]
  (let [next-offset (+ offset @per-page)
        has-more? (> (repo/count-by-regex query) next-offset)]
    (if has-more? next-offset "")))

(defn- inline-handler [{:keys [id query offset]}]
  (if-not (nil? id)
    (let [parsed-offset (parse-offset offset)
          next-offset (next-offset parsed-offset query)
          result (repo/find-by-regex parsed-offset query)]
      (t/answer-inline @token id
                       {:next_offset next-offset}
                       (apply-additional-fields result)))))

(h/defhandler bot-api
  (h/inline-fn inline-handler))
