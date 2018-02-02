(ns machinery.api.vk
  "Wrapper around official VK SDK."
  (:require [environ.core :refer [env]]
            [clojure.string :refer [split]]
            [clojure.data.json :as json])
  (:import com.vk.api.sdk.client.actors.ServiceActor
           com.vk.api.sdk.client.VkApiClient
           com.vk.api.sdk.httpclient.HttpTransportClient
           com.google.gson.Gson))

(def vk-app-id
  (delay (-> (env :vk-app-id)
             (Integer.))))

(def vk-service-key
  (delay
   (env :vk-service-key)))

(def actor
  (delay
   (new ServiceActor @vk-app-id @vk-service-key)))

(def api
  (->> (new HttpTransportClient)
       (new VkApiClient)))

(defn deserialize
  "Thanks VK, but I don't need your fancy Response wrapper."
  [item]
  (-> (new Gson)
    (.toJson item)))

(defn- api-params-cast-item [item]
  (if (number? item)
    (Integer. item)
    item))

(defn- api-params [params]
  (map
   (fn [[k v]] (list (.-sym k) (api-params-cast-item v)))
   params))

(defmacro vk [method params]
  (let [[entity action] (split (name method) #"\.")
        api-params (api-params params)]
    `(-> (. api ~(symbol entity))
         (. ~(symbol action) @actor)
         (.. ~@api-params)
         (.execute)
         (deserialize)
         (json/read-str))))
