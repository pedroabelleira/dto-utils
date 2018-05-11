(ns dto.util.util
  (:require [clojure.reflect :as ref]
            [clojure.string :as st]))


(defn- type-short-description [type]
  "Provides a short version of the type name"
  (if (nil? type)
    ""
    (-> type
        (st/replace "clojure.lang." "")
        (st/replace "java.lang." "")
        (st/replace "java.util." ""))))

(defn- member-desc [member]
  "Builds a description for a given method"
  (let [name   (:name member)
        rtype  (:return-type member)
        pars   (:parameter-types member)
        excps  (:exception-types member)]
    (str
     name
     "("
     (st/join " " (map type-short-description pars))
     "): "
     (type-short-description rtype)
     (if (> 0 (count excps))
       (str
        " throws "
        (map type-short-description excps))))))


(defn omethods [ob]
  "Returns a list of strings describing the methods of the object ob"
  (let [des (ref/reflect ob)
        members (:members des)]
    (map member-desc members)))
