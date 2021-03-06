(ns dto.util-test
  (:import dto.api.IPerson)
  (:require [clojure.test :refer :all]
            [clojure.data :as data]
            [dto.util :refer :all :use-macros true]))

(defn- map-equal-in-key? [key m1 m2]
  (let [v1 (key m1)
        v2 (key m2)]
    (= v1 v2)))

(defn submap? [small big]
  (println "Call to submap? " small big)
  (let [keys (keys small)]
    (reduce #(and %1 %2) (map #(map-equal-in-key? % small big) keys))))

(comment
  (deftest a-test
    (testing "Dto simple"
      (let [d {:name "John" :sur-name "Doe"}
            p (defdto IPerson d)]
        (is (submap? d (bean p)))))))

(deftest interface-test
  (testing "interface?"
    (is (#'dto.util/interface? java.util.Collection))))

(deftest build-key-test
  (testing "build-key"
    (is (#'dto.util/build-key "getFirstName") :first-name)
    (is (#'dto.util/build-key "getFirstOrLastName") :first-or-last-name)))

(deftest name-iface-test
  (testing "name-iface"
    (is (#'dto.util/name-iface java.util.Collection) "Collection")
    (is (#'dto.util/name-iface java.util.ArrayList) "ArrayList")))

(deftest map->dto-test1
  (testing "map->dto"
    (let [orig {:name "John" :sur-name "Doe"}]
      (is
       (nil?
        (first
         (data/diff orig (dto->map (map->dto orig dto.api.IPerson)))))))))


(deftest map->dto-test1
  (testing "map->dto"
    (let [orig {:name "John" :sur-name "Doe" :address {:street "Rue de L'angle" :number "42"}}]
      (is
       (nil?
        (first
         (data/diff orig (dto->map (map->dto orig dto.api.IPerson)))))))))

(deftest kebab-camel
  (testing "kebab->camel"
    (is (= "full-name" (camel->kebab "fullName")))
    (is (= "first-named-address" (camel->kebab "firstNamedAddress"))))
  (testing "camel->kebab"
    (is (= "fullName" (kebab->camel "full-name")))
    (is (= "firstNamedAddress" (kebab->camel "first-named-address")))))
