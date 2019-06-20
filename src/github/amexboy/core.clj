(ns github.amexboy.core
  (:require [clojure.tools.cli :refer [parse-opts] :as opts])
  (:use [github.amexboy.modules-loader]
        [clojure.string :only [split]])
  (:gen-class))

(def cli-options
  ;; An option with a required argument
  [["-l" "--list" "List modules"]
   ["-B" "--base-branch Branch" "Base branch"
    :default "origin/master"
    ]
   [nil "--no-clone" "Don't try to clone repository"]
   ["-r" "--repository-root repositories-root" "Use https to clone repository. Ignored if --no-clone is specified. (Such as git:github.com:/amexboy https://bitbucket.org/amexboy ssh://a:a@example.com/root )"]
   ["-s" "--source-root source-root-dir" "Directory where the repositories are cloned to. Default to current directory"
    :default "./"]
   ["-M" "--modules-root modules-root-dir" "Directory where projects are created. Default: modules relative to --source-root-dir"
    :default "modules"]
   ["-m" "--modules modules-to-proces" "Comma separated list of modules to add to the project. Will clone from repository if --no-clone is not specified and the module is not found in the --source-root"
    :parse-fn #(split % #",")]
   ["-b" "--branch branch-to-checkout" "The branch the will be checked out in all the listed modules. Branch will be created it it doesn't exist"]
   ["-n" "--name project-name" "The name of the project. This will also be the directory name (spaces replaced by _)"]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :update-fn inc]                                         ; Prior to 0.4.1, you would have to use:
   ;; :assoc-fn (fn [m k _] (update-in m [k] inc))
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn validate-options [options]
  (every? #(options %) [:branch :modules]))

(defn dispatch
  [{:keys [list source-root name branch modules] :as options}]
  (if list
    (list-modules source-root)
    (create-project  source-root (coalesce name branch) branch modules options)))

(defn -main [& args]
  (let [{:keys [options errors summary]} (opts/parse-opts args cli-options :strict true)]
    (cond
      (or errors (:help options)) (println errors "\n Usage: \n" summary)
      (validate-options options) (dispatch options)
      :default (println "Missing either branch or modules\n Usage: \n" summary)
      ))
  )


