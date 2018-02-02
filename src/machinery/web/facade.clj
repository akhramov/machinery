(ns machinery.web.facade
  (:require [machinery.web.facade.vk :as vk]
            [machinery.web.facade.telegram :as tg]
            [machinery.web.facade.repository :as repo]
            [environ.core :refer [env]]
            [ring.middleware.json :refer [wrap-json-params]])
  (:use ring.logger
        ring.adapter.jetty))

(defn- text-response [handler params]
  (-> {:status 200
       :headers {"Content-Type" "text/html"}
       :body (handler params)}))

(defn handler [{uri :uri params :params} respond raise]
  (respond
   (condp = uri
     (env :vk-uri) (text-response vk/handler params)
     (env :tg-uri) (text-response tg/handler params)
     (repo/handler uri))))

(defn run []
  "Launch webhooks facade"
  (-> handler
      (wrap-json-params)
      (run-jetty {:port 8080 :async? true})))
