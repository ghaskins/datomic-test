(ns datomic-test.core
  (:refer-clojure :exclude [print update get])
  (:require [clojure.tools.cli :refer [parse-opts]]
            [datomic-test.document :refer :all :as doc]
            [clojure.pprint :refer :all])
  (gen-class))

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
  (let [uri (str "datomic:free://localhost:4334/" (java.util.UUID/randomUUID))
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

    (pprint (doc/get-keys conn "foo" 1))
    (pprint (doc/get-keys conn "foo" 2))
    (datomic.api/release conn)
    (datomic.api/delete-database uri)
    ))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 summary))
    (run)
    (datomic.api/shutdown true)
    ))
