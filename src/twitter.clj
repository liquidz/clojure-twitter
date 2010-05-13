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

(defn- twitter4j-tweet-convert [t]
  (struct tweet
          (.getCreatedAt t) (.getFromUser t) (.getFromUserId t)
          (.getId t) (.getIsoLanguageCode t) (.getProfileImageUrl t)
          (.getSource t) (.getText t) (.getToUser t) (.getToUserId t)
          )
  )

(defn- twitter4j-query-result-convert [q]
  (struct query-result
          (.getMaxId q) (.getPage q) (.getQuery q) (.getRefreshUrl q)
          (.getResultsPerPage q) (.getSinceId q)
          (map twitter4j-tweet-convert (.getTweets q))
          (.getWarning q)
          )
  )

; =twitter-search
(defnk twitter-search [query :page -1 :since-id -1]
  (let [q (Query. query)]
    (if (pos? page) (.setPage q page))
    (if (pos? since-id) (.setSinceId q since-id))

    (twitter4j-query-result-convert
      (.. (TwitterFactory.) getInstance (search q))
      )
    )
  )


