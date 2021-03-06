(ns twitter
  (:import (twitter4j TwitterFactory Query))
  (:import (twitter4j.http AccessToken))
  (:use [simply core])
  (:require [clojure.contrib.string :as st])
  )

(def *twitter-result-per-page* 100)
(def *twitter-sleep-time* 1500)

; definition struct {{{
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
; }}}

; =sleep
(defn- sleep [ms] (Thread/sleep ms))

; converter {{{
; =twitter4j-tweet-convert
(defn- twitter4j-tweet-convert [t]
  (struct tweet
          (.getCreatedAt t) (.getFromUser t) (.getFromUserId t)
          (.getId t) (.getIsoLanguageCode t) (.getProfileImageUrl t)
          (.getSource t) (.getText t) (.getToUser t) (.getToUserId t)
          )
  )

; =twitter4j-status-convert
(defn- twitter4j-status-convert [s]
  (let [user (.getUser s)]
    (struct tweet
            (.getCreatedAt s) (.getScreenName user) (.getId user)
            (.getId s) "" (.toString (.getProfileImageURL user))
            (.getSource s) (.getText s) (.getInReplyToScreenName s) (.getInReplyToUserId s))
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
; }}}

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

; =get-twitter-instance
(defn get-twitter-instance [] (.getInstance (TwitterFactory.)))

; =show-twitter-status
(defn show-twitter-status [id]
  (twitter4j-status-convert
    (.showStatus (get-twitter-instance) id)
    )
  )

; =twitter-search
(defnk twitter-search [query :page -1 :since-id -1 :rpp -1 :locale "ja" :lang ""]
  (let [q (Query. query)]
    (when (pos? page) (.setPage q page))
    (when (pos? since-id) (.setSinceId q since-id))
    (when (pos? rpp) (.setRpp q rpp))
    (when-not (st/blank? locale) (.setLocale q locale))
    (when-not (st/blank? lang) (.setLang q lang))

    (twitter4j-query-result-convert
      (.search (get-twitter-instance) q)
      )
    )
  )

; =twitter-search-all
(defn twitter-search-all [& args]
  (loop [page 1, res nil]
    (let [qr (apply twitter-search (concat args (list :page page :rpp *twitter-result-per-page*)))]
      (if (nil? qr) res
        (if (< (count (:tweets qr)) *twitter-result-per-page*)
          (if (nil? res) qr (combine-query-result res qr))
          (do
            (sleep *twitter-sleep-time*)
            (recur (inc page) (if (empty? res) qr (combine-query-result res qr)))
            )
          )
        )
      )
    )
  )

; =get-twitter-rage-limit
(defn get-twitter-rate-limit []
  (let [res (.getRateLimitStatus (get-twitter-instance))]
    {:remaining-hits (.getRemainingHits res)
     :hourly-limit (.getHourlyLimit res)
     :reset-time-in-seconds (.getResetTimeInSeconds res)
     :reset-time (.getResetTime res)
     :seconds-until-reset (.getSecondsUntilReset res)
     }
    )
  )

; =get-twitter-oauth-access-token
(defn get-twitter-oauth-access-token  [twitter-instance request-token & pin]
  (let [access-token (if (zero? (count pin))
                       (.getOAuthAccessToken twitter-instance request-token)
                       (.getOAuthAccessToken twitter-instance request-token (first pin)))]
    [access-token twitter-instance]
    )
  )


; =get-twitter-oauth-url
(defn get-twitter-oauth-url
  ([twitter-instance]
   (let [request-token (.getOAuthRequestToken twitter-instance)
         auth-url (.getAuthorizationURL request-token)
         ]
     [auth-url request-token twitter-instance]
     )
   )
  ([consumer-key consumer-secret]
   (let [tw (get-twitter-instance)]
     (.setOAuthConsumer tw consumer-key consumer-secret)
     (get-twitter-oauth-url tw)
     )
   )
  )

; =twitter-logined?
(defn twitter-logined? [twitter-instance]
  (.isOAuthEnabled twitter-instance)
  )

; =get-twitter-screen-name
(defn get-twitter-screen-name [twitter-instance]
  (if (twitter-logined? twitter-instance)
    (.getScreenName twitter-instance)
    nil)
  )

; =get-twitter-profile-image-url
(defn get-twitter-profile-image-url [twitter-instance]
  (.. (.showUser twitter-instance (.getId twitter-instance)) getProfileImageURL toString)
  )

; =twitter-update
(defn twitter-update [twitter-instance s]
  (when (and (twitter-logined? twitter-instance) (string? s))
    (.updateStatus twitter-instance s)
    )
  )

; =get-twitter-authorized-instance
(defn get-twitter-authorized-instance [consumer-key consumer-secret access-token access-token-secret]
  (.getOAuthAuthorizedInstance
    (TwitterFactory.)
    consumer-key consumer-secret
    (AccessToken. access-token access-token-secret))
  )
