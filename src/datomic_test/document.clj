(ns datomic-test.document)

(use '[datomic.api :only [q db] :as d])
(use 'datomic-test.schema)
(use 'clojure.pprint)

(defn create-db [uri]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (datomic-test.schema/install conn)
    conn))

(defn get [id conn]
  (let [result (q '[:find (pull ?doc [*]) :in $ ?id :where [?doc :document/id ?id]] (db conn) id)
        nr (count result)]
    (if (not= 1 nr)
      (throw (Exception. (format "unexpected result count: %d", nr))))
    (ffirst result)))

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

(defn xlate-ops [id docid operations]
  (map #(if-let [value (:value %)]
          [:update-entry id docid (:name %) value]
          [:remove-entry id docid (:name %)])
       operations)
  )

(defn update [conn docid operations & opts]
  (let [id (d/tempid :db.part/user)
        tx-data (concat [[:inc-version id docid]] (xlate-ops id docid operations))]
    (pprint tx-data)
    (flush)
    (pprint (d/transact conn tx-data))
    )

  )

(defn commit [conn id]
  )
