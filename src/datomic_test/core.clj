(ns datomic-test.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(use '[datomic.api :only [q db] :as d])
(use 'datomic-test.schema)

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

(defn create-db [uri]
  (d/create-database uri)
  (let [conn (d/connect uri)]
      (d/transact conn datomic-test.schema/schema)
      conn))

(defn run []
  (let [conn (create-db "datomic:mem://foo")
        result (d/transact conn [{:db/id #db/id[:db.part/user -1000001],
                                  :entry/name "bar"
                                  :entry/value (.getBytes "baz")}
                                 {:db/id #db/id[:db.part/user],
                                  :document/id "foo",
                                  :document/version 1
                                  :document/entries #db/id[:db.part/user -1000001]}
                                 ])]
    (print result)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 summary))
    (run)
    ))
