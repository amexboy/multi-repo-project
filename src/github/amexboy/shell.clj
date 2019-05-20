(ns github.amexboy.shell
  (:require [clojure.java.shell :as sh])
  (:use [clojure.pprint]))


(defn sh [& args]
    (let [result (apply sh/sh args)]
      (pprint result)
      (:exit result)))