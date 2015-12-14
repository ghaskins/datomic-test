(ns datomic-test.schema)

(use '[datomic.api :only [q db] :as d])

(def inc-version
  "Atomically increment the document version"
  #db/fn {:lang :clojure
          :params [db id docid]
          :code (let [version (if-let [e (get db docid)]
                                (inc (:document/version e))
                                1)]
                  {:db/id id
                   :document/id docid
                   :document/version version})})

(defn install [conn]
  (d/transact conn
              [
               ;;------------------------------------------------------
               ;; install our schema
               ;;------------------------------------------------------
               ;; document schema
               {:db/id #db/id[:db.part/db]
                :db/ident :document/id
                :db/valueType :db.type/string
                :db/cardinality :db.cardinality/one
                :db/unique :db.unique/identity
                :db/index true
                :db/doc "A unique identifier for this document"
                :db.install/_attribute :db.part/db}
               {:db/id #db/id[:db.part/db]
                :db/ident :document/version
                :db/valueType :db.type/long
                :db/cardinality :db.cardinality/one
                :db/doc "The version of this document after commit"
                :db.install/_attribute :db.part/db}
               {:db/id #db/id[:db.part/db]
                :db/ident :document/entries
                :db/valueType :db.type/ref
                :db/cardinality :db.cardinality/many
                :db/isComponent true
                :db/doc "Name/value pair entries attached to this document"
                :db.install/_attribute :db.part/db}

               ;; entry (name/value pair) schema
               {:db/id #db/id[:db.part/db]
                :db/ident :entry/name
                :db/valueType :db.type/string
                :db/cardinality :db.cardinality/one
                :db/index true
                :db/doc "Name (key) of this entry"
                :db.install/_attribute :db.part/db}
               {:db/id #db/id[:db.part/db]
                :db/ident :entry/value
                :db/valueType :db.type/bytes
                :db/cardinality :db.cardinality/one
                :db/doc "(Opaque) value of this entry"
                :db.install/_attribute :db.part/db}

               ;;------------------------------------------------------
               ;; install our transaction-functions
               ;;------------------------------------------------------
               {:db/id #db/id[:db.part/user]
                :db/ident :inc-version
                :db/fn inc-version}]))
