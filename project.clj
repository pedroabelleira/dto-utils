(defproject dto "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [korma "0.4.3"]
                 [org.clojure/java.jdbc "0.7.6"]
                 [com.h2database/h2 "1.4.192"]]
  :main ^:skip-aot dto.core
  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
