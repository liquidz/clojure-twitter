(ns twitter
  (:import (twitter4j TwitterFactory Query))
  (:use simply)
  (:require [clojure.contrib.str-utils2 :as su2])
  )

(defstruct
  tweet
  :created-at :from-user :from-user-id :id :iso-language-code
  :profile-image-url :source :text :to-user :to-user-id
  )

(defstruct
  query-result
  :max-id :page :query :refresh-url :results-par-page
  :since-id :tweets :warning
  )

(def *twitter-result-per-page* 100)
(def *twitter-sleep-time* 1500)

; =sleep
(defn- sleep [ms] (Thread/sleep ms))

; =twitter4j-tweet-convert
(defn- twitter4j-tweet-convert [t]
  (struct tweet
          (.getCreatedAt t) (.getFromUser t) (.getFromUserId t)
          (.getId t) (.getIsoLanguageCode t) (.getProfileImageUrl t)
          (.getSource t) (.getText t) (.getToUser t) (.getToUserId t)
          )
  )

; =twitter4j-query-result-convert
(defn- twitter4j-query-result-convert [q]
  (struct query-result
          (.getMaxId q) (.getPage q) (.getQuery q) (.getRefreshUrl q)
          (.getResultsPerPage q) (.getSinceId q)
          (map twitter4j-tweet-convert (.getTweets q))
          (.getWarning q)
          )
  )

; =combine-query-result
(defn- combine-query-result [qr1 qr2]
  (assoc qr1
         :page (:page qr2)
         :max-id (max (:max-id qr1) (:max-id qr2))
         :refresh-url (:refresh-url qr2)
         :tweets (concat (:tweets qr1) (:tweets qr2))
         :warning (:warning qr2)
         )
  )

; =twitter-search
(defnk twitter-search [query :page -1 :since-id -1 :rpp -1 :locale "ja" :lang ""]
  (let [q (Query. query)]
    (if (pos? page) (.setPage q page))
    (if (pos? since-id) (.setSinceId q since-id))
    (if (pos? rpp) (.setRpp q rpp))
    (if (! su2/blank? locale) (.setLocale q locale))
    (if (! su2/blank? lang) (.setLang q lang))

    (twitter4j-query-result-convert
      (.. (TwitterFactory.) getInstance (search q))
      )
    )
  )

; =twitter-search-all
(defn twitter-search-all [& args]
  (loop [page 1, res ()]
    (let [qr (apply twitter-search (concat args (list :page page :rpp *twitter-result-per-page*)))]
      (if (< (count (:tweets qr)) *twitter-result-per-page*)
        (if (empty? res) qr (combine-query-result res qr))
        (do
          (sleep *twitter-sleep-time*)
          (recur (++ page) (if (empty? res) qr (combine-query-result res qr)))
          )
        )
      )
    )
  )

(comment
(defn get-oauth-access-token [consumer-key consumer-secret]
  )
(defn oauth [consumer-key consumer-secret]
  (let [tw (.. (TwitterFactory.) getInstance)]
    (.setOAuthConsumer tw consumer-key consumer-secret)
    (let [request-token (.getOauthRequestToken tw)
          auth-url (.getAuthorizationURL request-token)
          ]
      )
    )
  )
  )

