(ns dto.core
  (:gen-class)
  (:require [clojure.reflect :as ref]
            [clojure.string  :as st]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn- is-getter? [method]
  (and (is-void? method)
       (is-method? method)
       (st/starts-with? (str (:name method)) "get")))

(defn- is-method? [method]
  (isa? clojure.reflect.Method (class method)))

(defn- is-void? [method]
  (= 0 (count (:parameter-types method))))

(defn- find-interface-getters [iface]
  (let [info (ref/reflect iface)
        members (:members info)
        meths (filter is-getter? members)]
    meths))

(defn- to-kebab-case [ch]
  (if (= (str ch) (st/capitalize ch))
    (str "-" (st/lower-case ch))
    ch))

(defn- build-key [name]
  (st/replace 
   (st/join (map to-kebab-case name))
   "get-"
   ""))

(defn- create-method-form [method obj]
  ;(println "Call to create-method-form [" method " " obj "]")
  (let [name (:name method)
        key (build-key (str name))]
    `(~name [~'_] (~(keyword key) ~obj))))

(defmacro defdto [iface obj]
  (->>
   (map #(create-method-form % obj) (find-interface-getters (eval iface)))
   (cons iface)
   (cons 'reify)))

