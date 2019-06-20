(ns github.amexboy.modules-loader
  (:require [clojure.java.io :as io])
  (:use [clojure.java.shell :only [sh]]
        [clojure.string :only [split]]
        [github.amexboy.git-ops]
        [github.amexboy.project-gen])
  (:gen-class))

(defn coalesce
  "Returns first non-nil argument."
  [& args]
  (first (keep identity args)))

(defn list-modules [root]
  "Returns a list of module name in this root"
  (let [dir (io/file root)]
    (filter maven-root? (.listFiles dir)))
  )

(defn pull-module [root module config]
  "Pull module from github"
  (when-not (:no-clone config)
    (git-clone (str (:repository-root config) module) root)))

(defn update-repository [root module config]
  "Checks if module exists and updates the repository, pull from github if not"
  (let [module-file (io/file root module)]
    (if-not (.exists module-file)
      (pull-module root module config)
      true)))



(defn create-module [root modulein branch name opts]
  (let [module-split (split modulein #"|")
        module (first module-split)
        branch-part (last module-split)
        module-dir (str module
                        (if (> (count module-split) 1)
                          (clojure.string/replace branch-part #"/" "-")))
        base-branch (coalesce branch-part (:base-branch opts))
        src (.getPath (io/file root module))
        work-tree-file (io/file root (:modules-root opts) name module-dir)
        work-tree (.getPath work-tree-file)]
    (if (update-repository root module opts)
      (do
        (println module)
        (git-add-worktree src branch work-tree base-branch)
        (cond
          (maven-root? work-tree-file) (gen-maven-iml work-tree module)
          (gradle-root? work-tree-file) (gen-gradle-iml work-tree module)
          (liengen-root? work-tree-file) (gen-liengen-iml work-tree module)
          )
        module)
      false)))

(defn create-project
  "Creates a new ipr file and loads the module checking out the branch on the modules"
  [root name branch modules opts]
  (gen-ipr (io/file root (:modules-root opts) name)
           name
           (filter identity (map #(create-module root % branch name opts) modules))
           opts))

(defn update-project [ipr new-modules]
  "Adds the specified module to the IPR"
  )

