(ns core
  (:require [org.httpkit.server :as httpkit]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.coercion.malli]
            [reitit.ring.coercion :as rrc]
            [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn read-config []
  (with-open [r (io/reader (io/resource "config.edn"))]
    (edn/read (java.io.PushbackReader. r))))

(defonce config (read-config))

(defn create-user [{:keys [path-params]}]
  (let [user-id (get path-params :user-id)]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (str "Successfully created user with ID: " user-id)}))

(defn get-user [{:keys [path-params]}]
  (let [user-id (get path-params :user-id)]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (str "User ID requested: " user-id)}))

(defn root [{:keys [headers]}]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string headers)})

(def router
  (ring/router
   [["/" {:get root}]
    ["/create/:user-id"
     {:get {:handler get-user
            :parameters {:path {:user-id int?}}}
      :post {:handler create-user
             :parameters {:path {:user-id int?}}}}]]
   {:data {:coercion reitit.coercion.malli/coercion
           :middleware [parameters/parameters-middleware
                        rrc/coerce-request-middleware
                        wrap-keyword-params]}}))

(def app
  (ring/ring-handler
   router
   (ring/create-default-handler)))

(defn -main [& _]
  (println "Starting server on port 8080")
  (println "Loaded config: " config)

  (httpkit/run-server app {:port 8080}))