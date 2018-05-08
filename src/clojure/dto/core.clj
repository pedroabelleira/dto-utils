(ns dto.core
  (:gen-class)
  (:import dto.api.IPerson)
  (:require [dto.util :refer :all]
            [clojure.java.jdbc :as jdbc]))

(def db {:classname   "org.h2.Driver" 
         :url         "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000"
         :user     "sa"
         :password ""})
(def db-spec
  {:connection-uri "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000"
   :user           "sa"
   :password        ""})

(defn- create-db []
  (let [sql (slurp "/home/pedro/dev/clojure/dto-utils/resources/db-create.sql")]
    (jdbc/execute! db-spec sql)))

(defn- user-by-id [id]
  (let [sql (str "select * from OIBADMIN_ROLES where role_id = '" id "'")]
    (jdbc/query db-spec sql)))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [p (defdto IPerson {:name "Pedro" :sur-name "Abelleira Seco"})]
    (create-db)
    (println (user-by-id 1))
    (println "Hello," (.getName p) "!")))

