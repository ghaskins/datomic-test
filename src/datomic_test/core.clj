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

(defn create []
  (let [conn (doc/create-db "datomic:mem://foo")]
    (doc/update conn "foo"
                [{:name "bar" :value (.getBytes "baz")}
                 {:name "bat" :value (.getBytes "bah")}])
    (doc/update conn "bar"
                [{:name "bar" :value (.getBytes "baz")}
                 {:name "bat" :value (.getBytes "bah")}])

    conn))

(defn run []
  (let [conn (create)]
    (doc/print (doc/get "foo" conn))
    (doc/print (doc/get "bar" conn))))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 summary))
    (run)
    ))
