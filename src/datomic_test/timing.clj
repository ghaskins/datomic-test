(ns datomic-test.timing)

(defn once [f & args]
  (let [start  (System/nanoTime)
        result (apply f args)
        stop   (System/nanoTime)]
    (vector result (- stop start))))

(defn many [nr f & args]
  (let [[_, result] (once #(loop [i 1]
                                 (apply f args)
                                 (if (> i nr)
                                   nil
                                   (recur (inc i)))))]
    (double (/ result nr))))
