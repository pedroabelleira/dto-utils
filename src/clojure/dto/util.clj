(ns dto.util
  (:gen-class)
  (:require [clojure.reflect :as ref]
            [clojure.string  :as st]))

(defn- has-empty-parameter-list? [method]
  (= 0 (count (:parameter-types method))))

(defn- is-method? [method]
  (isa? clojure.reflect.Method (class method)))

(defn- is-getter? [method]
  (and (has-empty-parameter-list? method)
       (is-method? method)
       (st/starts-with? (str (:name method)) "get")))

(defn- find-interface-getters [iface]
  "Returns all the getter methods (name starts by 'get', has no parameters, ...) of the given interface iface"
  (let [info (ref/reflect iface)
        members (:members info)
        meths (filter is-getter? members)]
    meths))

(defn- to-kebab-case [ch]
  "Converts a caracter /inside/ a word to the corresponding character(s) in kebab case ('C' -> '-c')"
  (if (= (str ch) (st/capitalize ch))
    (str "-" (st/lower-case ch))
    ch))

(defn- build-key [name] 
  "Builds a key in kebab case for the given name (in camel case)"
  (st/replace 
   (st/join (map to-kebab-case name))
   "get-"
   ""))

(defn- create-method-form [method obj]
  (let [name (:name method)
        key (build-key (str name))]
    `(~name [~'_] (~(keyword key) ~obj))))

(defmacro defdto [iface obj]
  "Defines an object which implements the interface iface by returning to any call
   of format getAbcXyz(void)  the value (:abc-xyz obj)"
  (->>
   (map #(create-method-form % obj) (find-interface-getters (eval iface)))
   (cons iface)
   (cons 'reify)))

