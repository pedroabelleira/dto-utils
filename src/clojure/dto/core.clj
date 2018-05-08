(ns dto.core
  (:gen-class)
  (:import dto.api.IPerson)
  (:require [dto.util :refer :all]
            [clojure.java.jdbc :as j]))

;; (def conn )
;; (defn- create-db []
;;   (let [sql (slurp "resources/db-create.sql")]


;;   )

;; (defn- destroy-db [conn]
;;   )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [p (defdto IPerson {:name "Pedro" :sur-name "Abelleira Seco"})]
    (println "Hello," (.getName p) "!")))


