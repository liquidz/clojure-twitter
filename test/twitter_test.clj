(ns twitter-test
  (:use simply)
  (:use twitter)
  (:use [clojure.test])
  )

(deftest mytest-test
  (let [sr (twitter-search "#teatime")]
    (println "max id = " (:max-id sr))
    ;(foreach #(println (to-utf8 (:text %)) " (id = " (:id %) ")") (:tweets sr))
    )

  (let [res (twitter-search-all "#teatime" :lang "ja")]
    (println "max id = " (:max-id res))
    (println "page = " (:page res))
    (println "tweets count = " (count (:tweets res)))
    ;(foreach #(println (to-utf8 (:text %))) (take 10 (:tweets res)))
    )
  )
