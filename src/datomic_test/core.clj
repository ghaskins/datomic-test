(ns datomic-test.core
  (:refer-clojure :exclude [print update get])
  (:require [clojure.tools.cli :refer [parse-opts]]
            [datomic.api :refer [q db] :as d]
            [datomic-test.document :refer :all :as doc]
            [datomic-test.timing :refer :all :as timing]
            [clojure.pprint :refer :all])
  (gen-class))

(def cli-options
  ;; An option with a required argument
  [["-i" "--iterations COUNT" "Iteration count"
    :default 100
    :parse-fn #(Integer/parseInt %)
    :validate [#(> % 0)]]
   ["-h" "--help"]])

(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn simpletest []
  (let [uri "datomic:mem:/foo"
        conn (doc/create-db uri)]

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
    (datomic.api/delete-database uri)
    ))

(defn run [conn options]
  (let [nr (:iterations options)
        tests [["null" #(str %)]
               ["doc/add-entry" #(doc/update conn "foo"
                                             [{:name (str %)
                                               :value (.getBytes "blah")}])]
               ["raw/insert" #(d/transact conn [{:db/id (d/tempid :db.part/user)
                                                 :document/id (str "raw-insert-" %)}])]
               ["raw/async-insert" #(d/transact-async conn [{:db/id (d/tempid :db.part/user)
                                                             :document/id (str "async-insert-" %)}])]]]

    (dorun (map (fn [[name func]]
                  (let [[_ result] (timing/once #(dotimes [i nr] (func i)))]
                        (println name ":" (double (/ (/ result nr) 1000)) "us/iteration")))
                tests))))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 summary))

    (println "Starting datomic-test with" (:iterations options) "iterations")
    (let [uri (str "datomic:free://localhost:4334/" (java.util.UUID/randomUUID))
          conn (doc/create-db uri)]
      (run conn options)
      (datomic.api/release conn)
      (datomic.api/delete-database uri))
    (datomic.api/shutdown true)))
