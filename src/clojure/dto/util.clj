(ns dto.util
  "Macro and related utilities to implement Java interfaces from
  clojure structures"
  ;(:gen-class)
  (:require [clojure.reflect :as ref]
            [clojure.string  :as str]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utility functions 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- has-empty-parameter-list? [method]
  (= 0 (count (:parameter-types method))))

(defn- interface? [clazz]
  (-> (ref/reflect clazz) :flags (contains? :interface)))

(defn- method? [method]
  (isa? clojure.reflect.Method (class method)))

(defn- getter? [method]
  (and (method? method)
       (has-empty-parameter-list? method)
       (str/starts-with? (str (:name method)) "get")))

(defn- find-interface-getters
  "Returns all the getter methods (name starts by 'get', has no parameters, ...) of
  the given interface iface"
  [iface]
  (let [info    (ref/reflect iface)
        members (:members info)
        meths   (filter getter? members)]
    meths))

(defn- kebab->camel-reductor [acc next]
  (if (= \- (last acc))
    (str acc (clojure.string/capitalize next)) ; FIXME: A bit hackish...
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
  (keyword (str/replace (camel->kebab name) "get-" "")))

(defn- name-iface
  "Returns the name of the interface iface as a string"
  [iface]
  (->> (partition-by #(= \. %) (ref/typename iface))
       (map str/join)
       (last)))

(defn- package-iface
  "Returns the package of the interface iface as a string"
  [iface]
  (->> (partition-by #(= \. %) (ref/typename iface))
       (map str/join)
       (butlast)
       (butlast)
       (str/join)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions used in macro expansion
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- apply-transform [f form] ; Applies a transformation f to a given clojure form
  (if (nil? f) form (f form)))

(defn- create-iface-pred
  "Creates the predicate to test whether an interface should be
  considered as representing a DTO, from an start predicate.
  If the predicate is nil, then the predicate will be set to
  'is the test interface in the same package as the given interface iface?'
  If the initial predicate is a string, the generated predicate will be
  'is the test interface in the package of the given string, or any
  subpackage of that one?'
  In any other case the predicate is returned unchanged"
  [iface & [pred]]
  (cond
    (nil? pred)
    (create-iface-pred iface (package-iface iface))

    (string? pred)
    (fn [test-iface] (str/starts-with? (package-iface iface)
                                       (package-iface test-iface)))
    :else pred))

(declare map->dto*)

(defn- create-getter-form
  "Core method: it creates the getter for a given property.
  It takes into account whether the property is an array or
  whether it is an object which should be mapped to another
  interface"
  [method obj iface-pred]
  (let [rtype1 (str (:return-type method)) ; Note the conversion back and forth from type to string...
        array? (str/ends-with? rtype1 "<>")
        rtype  (symbol
                (if array?
                  (str/replace rtype1 #"<>$" "")
                  rtype1))
        dto?   (iface-pred rtype) ; Here again 
        name   (:name method)
        key    (keyword (build-key (str name)))
        body1  `(~key ~obj)
        body   (if array?
                 (if dto?
                   `(into-array ~rtype (map #(map->dto* % ~rtype) ~body1))
                   `(into-array ~rtype ~body1))
                 (if dto?
                   (map->dto* `(~key ~obj) rtype)
                   body1))]
    `(~name [~'_] ~body)))

(defn map->dto*
  [obj iface & [iface-pred]]
  (let [iface-pred (create-iface-pred iface iface-pred)]
    `(reify ~iface
       ~@(map #(create-getter-form % obj iface-pred)
              (find-interface-getters (resolve iface))))))

(defmacro map->dto
  "Defines an object which implements the interface iface by returning to any
   call of format getAbcXyz(void)  the value (:abc-xyz obj)
   iface-pred represents the criteria for other interfaces found in
   the methods of the main interface to be treated as DTOs.
   iface-pred can be either a string or a function of one argument.
   When iface-pred is a string, all interfaces in any package which
   starts with string will be considered DTOs. If iface-pred is a function,
   if must have an argument which is the interface found and return true
   or false depending whether this interface is a DTO or not"
  [obj iface & iface-pred]
  (map->dto* obj iface iface-pred))

(declare read-dto-value)

(defn dto->map
  "Converts an object assumed to have been created by a call to map->dto
   to a map form. Similar to 'bean' but converts properties to kebab case"
  [o]
  (let [b (bean o)
        rawkeys (keys b)
        keys    (remove #(= :class %) rawkeys)] ; The dto adds a class property
    (reduce (fn [acc next]
              (assoc acc
                     (keyword (camel->kebab (name next)))
                     (read-dto-value (get b next))))
            {}
            keys)))

(defn- is-array? [ob]
  (if (nil? ob)
    false
    (.isArray (class ob))))

(defn- dto?
  "Returns true if the object passed seems to be a dto created by the map->dto macro"
  [ob]
  (if (or (nil? ob) (is-array? ob))
    false
    (let [dec-classes (set (as-> (ref/reflect ob) it (:members it) (map :declaring-class it)))]
      (and
       (= 1 (count dec-classes))
       (not (nil? (str/index-of (str dec-classes) "reify")))))))

(defn- read-dto-value [d]
  (cond
    (nil? d)      nil
    (dto? d)      (dto->map d)
    (is-array? d) (map read-dto-value (seq d))
    :else         d))

(comment
  ;; Useful code to use in the REPL
  (import dto.api.IPerson dto.api.IAddress dto.api.IGroup)
  (require '[dto.util :refer :all])
  (def m {:name "John" :sur-name "Doe" :aliases ["John Maddog Doe" "Johnny"] :address {:street "Rue" :number "42"}})

  ;; Example usage
  (def p (map->dto m dto.api.IPerson)) ; p now points to an object implementing the IPerson interface

  ;; Generated code can be checked with
  (macroexpand-1 '(map->dto m dto.api.IPerson))

  ;; Example code produced by the line above
  (clojure.core/reify
    dto.api.IPerson
    (getOtherAddresses
      [_]
      (clojure.core/into-array
       dto.api.IAddress
       (clojure.core/map
        (fn*
         [p1__19333__19334__auto__]
         (dto.util/map->dto* p1__19333__19334__auto__ dto.api.IAddress))
        (:other-addresses m))))
    (getAliases
      [_]
      (clojure.core/into-array java.lang.String (:aliases m)))
    (getSurName [_] (:sur-name m))
    (getAddress
      [_]
      (clojure.core/reify
        dto.api.IAddress
        (getNumber [_] (:number (:address m)))
        (getFullAddress [_] (:full-address (:address m)))
        (getStreet [_] (:street (:address m)))))
    (getName [_] (:name m))))
