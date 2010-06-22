(ns twitter-test
  (:use
     simply
     twitter
     clojure.test
     )
  (:require keys)
  )

(deftest mytest-test
;  (let [sr (twitter-search-all "#yuruyomi" :since-id 15562499949)]
;    (println "max id = " (:max-id sr))
;    ;(foreach #(println (to-utf8 (:text %)) " (id = " (:id %) ")") (:tweets sr))
;    (foreach #(println (to-utf8 (:text %)) " (id = " (:id %) ")")
;             (sort #(< (:id %1) (:id %2)) (:tweets sr)))
;    )

;  (let [res (twitter-search-all "#yuruyomi")]
;    (println "max id = " (:max-id res))
;    (println "page = " (:page res))
;    (println "tweets count = " (count (:tweets res)))
;    (foreach #(println (to-utf8 (:text %))) (take 10 (:tweets res)))
;    )

;  (foreach #(println (first %) " => " (second %)) (get-twitter-rate-limit))

  ;(p show-twitter-status 15562499950)

  (let [[url tw] (get-oauth-url keys/*twitter-consumer-key* keys/*twitter-consumer-secret*)]
    (println url)
    )
  )
