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
  (let [conn (create-db "datomic:mem://foo")]
    (print conn)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 summary))
    (run)
    ))
