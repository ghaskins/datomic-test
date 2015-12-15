(defproject datomic-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [com.datomic/datomic-free "0.9.5344"]]
  :main ^:skip-aot datomic-test.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :jvm-opts ^:replace ["-Xmx1g" "-server"])
