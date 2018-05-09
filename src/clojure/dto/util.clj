(ns dto.util
  (:gen-class)
  (:require [clojure.reflect :as ref]
            [clojure.string  :as st]
            [clojure.string :as str]))

(defn- has-empty-parameter-list? [method]
  (= 0 (count (:parameter-types method))))

(defn- is-method? [method]
  (isa? clojure.reflect.Method (class method)))

(defn- is-getter? [method]
  (and (has-empty-parameter-list? method)
       (is-method? method)
       (st/starts-with? (str (:name method)) "get")))

(defn- find-interface-getters
  "Returns all the getter methods (name starts by 'get', has no parameters, ...) of the given interface iface"
  [iface]
  (let [info (ref/reflect iface)
        members (:members info)
        meths (filter is-getter? members)]
    meths))

(defn kebab->camel-reductor [acc next]
  (if (= \- (last acc))
    (str acc (clojure.string/capitalize next)) ; FIXME
    (str acc next)))

(defn kebab->camel
  "Converts a word in kebab case to the corresponding character(s)
  in camel case ('first-name' -> 'firstName')"
  [s]
  (clojure.string/replace
   (reduce kebab->camel-reductor "" s)
   "-"
   ""))

(defn- upper-case? [c] (= (str/upper-case c) (str c)))

(defn- camel->kebab-reductor [acc next]
  (if (upper-case? next)
    (str acc "-" (str/lower-case next))
    (str acc next)))

(defn camel->kebab
  "Coverts a word in camel case to kebab case"
  [s]
  (reduce camel->kebab-reductor "" s))

(defn- build-key
  "Builds a key in kebab case for the given name (in camel case)"
  [name]
  (keyword (st/replace (camel->kebab name) "get-" "")))

(defn- create-direct-method-form [method obj]
  (let [name (:name method)
        key (build-key (str name))]
    `(~name [~'_] (~(keyword key) ~obj))))

(defn- create-method-form [method obj]
  (create-direct-method-form method obj))

(defmacro map->dto
  "Defines an object which implements the interface iface by returning to any call
   of format getAbcXyz(void)  the value (:abc-xyz obj)"
  [obj iface]
  (->>
   (map #(create-method-form % obj) (find-interface-getters (eval iface)))
   (cons iface)
   (cons 'reify)))

(defn dto->map [o]
  (let [b (bean o)
        keys (keys b)]
    (reduce (fn [acc next] (assoc acc
                                  (keyword (camel->kebab (name next)))
                                  (get b next)))
            {}
            keys)))
