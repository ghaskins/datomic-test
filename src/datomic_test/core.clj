(ns datomic-test.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(use '[datomic-test.document :as doc])

(def cli-options
  ;; An option with a required argument
  [["-i" "--iterations COUNT" "Iteration count"
    :default 10000
    :parse-fn #(Integer/parseInt %)
    :validate [#(> % 0)]]
   ["-h" "--help"]])

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn updateprint [conn id operations]
  (doc/update conn id operations)
  (doc/print (doc/get id conn))
  )

(defn run []
  (let [conn (doc/create-db "datomic:mem://foo")]
    ;; create our first version of "foo" with two entries
    (updateprint conn "foo"
            [{:name "bar" :value (.getBytes "baz")}
             {:name "bat" :value (.getBytes "bah")}])
    ;; now update "foo" to remove the "bar" entry
    (updateprint conn "foo" [{:name "bar"}])
    ))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 summary))
    (run)
    ))
