(ns datomic-test.document)

(use '[datomic.api :only [q db] :as d])
(use 'datomic-test.schema)
(use 'clojure.pprint)

(defn create-db [uri]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (datomic-test.schema/install conn)
    conn))

(defn get [conn id]
  (d/entity (db conn) [:document/id id]))

(defn getkeys [id version]
  )

(defn getvalues [id version pred]
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
