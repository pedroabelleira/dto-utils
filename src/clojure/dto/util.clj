(ns dto.util
  "Macro and related utilities to implement Java interfaces from
  clojure structures"
  (:gen-class)
  (:require [clojure.reflect :as ref]
            [clojure.string  :as st]
            [clojure.string  :as str]))

(defn- has-empty-parameter-list? [method]
  (= 0 (count (:parameter-types method))))

(defn interface? [clazz] ; FIXME: there must be a method for this
  (-> (ref/reflect clazz) :flags (contains? :interface)))

(defn- method? [method]
  (isa? clojure.reflect.Method (class method)))

(defn- getter? [method]
  (and (has-empty-parameter-list? method)
       (method? method)
       (st/starts-with? (str (:name method)) "get")))

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

(defn- create-array-method-form [method obj type]) ; FIXME: implement

(defn- create-direct-method-form
  "Method for properties which don't correspond array or
  mappable interfaces"
  [method obj]
  (let [name (:name method)
        key (build-key (str name))]
    `(~name [~'_] (~(keyword key) ~obj))))

(defn- dto-iface? [iface iface-pred]
  (let [iface-pred (if (nil? iface-pred)
                     (and (interface? iface)
                          (str/starts-with?) (ref/typename iface))
                     iface-pred)]))

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

(defn- create-method-form [method obj iface-pred]
  (let [return-type    (:return-type method)
        array?         (str/ends-with? method "<>")
        real-ret-type  (if array? (str/replace #"<>$" "") return-type)
        recur?         (iface-pred real-ret-type)]
    (println return-type array? real-ret-type recur?)
    (cond
      array? (println "Array found: not implemented")
      recur? (do (println "Interface DTO found: " real-ret-type " -> not implemented yet") (create-direct-method-form method obj))
      :else (create-direct-method-form method obj))))

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
  (let [iface-pred (create-iface-pred iface iface-pred)]
    (->>
     (map #(create-method-form % obj iface-pred)
          (find-interface-getters (eval iface)))
     (cons iface)
     (cons 'reify))))

(defn dto->map
  "Converts an object assumed to have been created by a call to map->dto
   to a map form. Similar to 'bean' but converts properties to kebab case"
  [o]

  (let [b (bean o)
        rawkeys (keys b)
        keys (remove #(= :class %) rawkeys)] ; The dto adds a class property
    (reduce (fn [acc next] (assoc acc
                                  (keyword (camel->kebab (name next)))
                                  (get b next)))
            {}
            keys)))

(comment
  (defn create-person [m] ; FIXME: remove once done
    (reify
      IPerson
      (getSurName [_] (:sur-name m))
      (getName [_] (:name m))
      (getAddress [_] (map->dto (:address m) dto.api.IAddress))
      dto.api.IGroup
      (getMembers [_] nil)))
  )


