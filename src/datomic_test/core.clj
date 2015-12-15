(ns datomic-test.core
  (:refer-clojure :exclude [print update get ])
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class)
  (:use [datomic-test.document :as doc]))

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


(defn run []
  (let [conn (doc/create-db "datomic:mem://foo")]
    
    ;; create our first version of "foo" with two entries
    (doc/update conn "foo"
                 [{:name "bar" :value (.getBytes "baz")}
                  {:name "bat" :value (.getBytes "bah")}])
    ;; now update "foo" to remove the "bar" entry (the lack of a :value denotes a removal)
    (doc/update conn "foo" [{:name "bar"}])
    ;; now update the "bat" field to a new value
    (doc/update conn "foo"
                 [{:name "bat" :value (.getBytes "blah")}])
    (doc/update conn "foo"
                 [{:name "bar" :value (.getBytes "blaz")}])

    ;; print the different versions of the document
    (dorun (map #(doc/print (doc/get conn "foo" %)) (range 1 5)))

    (datomic.api/release conn)
    ))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 summary))
    (run)
    (datomic.api/shutdown true)
    ))
