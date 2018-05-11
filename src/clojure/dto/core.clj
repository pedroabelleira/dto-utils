(ns dto.core
  (:gen-class)
  (:import dto.api.IPerson)
  (:require [dto.util :refer :all]
            [clojure.java.jdbc :as jdbc]
            [korma.core :refer :all]
            [korma.db :as kd]))

(def db-spec
  {:connection-uri "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=100000"
   :user           "sa"
   :password        ""})

(defn- create-db []
  (let [sql (slurp "/home/pedro/dev/clojure/dto-utils/resources/db-create.sql")]
    (jdbc/execute! db-spec sql)))

(defn- user-by-id [id]
  (let [sql (str "select * from oibadmin_roles where role_id = '" id "'")]
    (jdbc/query db-spec sql)))

(defentity
  roles
  (database db-spec)
  (entity-fields :role_id :name :description)
  (table :oibadmin_roles))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [p (map->dto {:name "John" :sur-name "Doe"} IPerson)]
    (create-db)
    (println "With SQL:" (user-by-id 1))
    ;(println "With Korma:" (select roles))
    (println "Hello," (.getName p) "!")))

