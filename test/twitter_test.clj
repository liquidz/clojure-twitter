(ns twitter-test
  (:use simply)
  (:use twitter)
  (:use [clojure.test])
  )

(deftest mytest-test
  (let [sr (twitter-search "#teatime" :since-id 13850550195)]
    (println "max id = " (:max-id sr))
    (foreach #(println (to-euc (:text %)) " (id = " (:id %) ")") (:tweets sr))
    )
  )
