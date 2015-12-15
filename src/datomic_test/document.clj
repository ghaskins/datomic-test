(ns datomic-test.document
  (:refer-clojure :exclude [print update get ])
  (:require  [datomic.api :refer [q db] :as d])
  (:use datomic-test.schema)
  (:use clojure.pprint)
  )

(defn create-db [uri]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (datomic-test.schema/install conn)
    conn))

(defn get-db [conn docid version]
  (if (= version :latest)
    ;; retrieving the latest is trivial
    (db conn)
    ;; retrieving a historical version will take a little more work..
    ;; we need to identify the specific transaction that was involved
    ;; in committing the update to the version of the document we are
    ;; interested in, and then use that with (d/as-of) to form the
    ;; snapshot view
    (let [txn (q '[:find ?txn .
                   :in $ ?docid ?version
                   :where
                   [?doc :document/id ?docid]
                   ;; ?op (fourth parameter) == true to capture assertions only
                   [?doc :document/version ?version ?txn true]]
                 (d/history (db conn)) docid version)]
      (d/as-of (db conn) txn))))

(defn get [conn docid version]
  (d/entity (get-db conn docid version) [:document/id docid]))

(defn get-keys [id version]
  )

(defn get-values [id version pred]
  )

(defn print [doc]
  (printf "document \"%s\" version %d with %d entries: \n"
          (:document/id doc)
          (:document/version doc)
          (count (:document/entries doc)))
  (dorun
   (map #(printf "\t%s=%s" (:entry/name %)
                 (with-out-str (pprint (:entry/value %))))
        (:document/entries doc))))

(defn update [conn docid operations & opts]
  (let [id (d/tempid :db.part/user)]
    (d/transact conn
                (concat [[:inc-version id docid]]
                        (map #(if-let [value (:value %)]
                                [:update-entry id docid (:name %) value]
                                [:remove-entry id docid (:name %)])
                             operations)))))

(defn commit [conn id]
  )
