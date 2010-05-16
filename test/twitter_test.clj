(ns twitter-test
  (:use simply)
  (:use twitter)
  (:use [clojure.test])
  )

(deftest mytest-test
  (comment
  (let [sr (twitter-search "#teatime")]
    (println "max id = " (:max-id sr))
    ;(foreach #(println (to-utf8 (:text %)) " (id = " (:id %) ")") (:tweets sr))
    )
    )

  (let [res (twitter-search-all "#yuruyomi")]
    (println "max id = " (:max-id res))
    (println "page = " (:page res))
    (println "tweets count = " (count (:tweets res)))
    (foreach #(println (to-utf8 (:text %))) (take 10 (:tweets res)))
    )
  )
