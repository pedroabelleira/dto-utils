(ns dto.util-test
  (:import dto.api.IPerson)
  (:require [clojure.test :refer :all]
            [dto.util :refer :all :use-macros true]))

(defn- map-equal-in-key? [key m1 m2]
  (let [v1 (key m1)
        v2 (key m2)]
    (= v1 v2)))

(defn submap? [small big]
  (println "Call to submap? " small big)
  (let [keys (keys small)]
    (reduce #(and %1 %2) (map #(map-equal-in-key? % small big) keys))))

(deftest a-test
  (testing "Dto simple"
    (let [d {:name "John" :sur-name "Doe"}
          p (defdto IPerson d)]
      (is (submap? d (bean p))))))
