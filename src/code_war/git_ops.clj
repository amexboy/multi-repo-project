(ns code-war.git-ops
  (:use [code-war.shell :only [sh]]))

(defn git-prune-worktree [root]
  "Removes dead worktrees"
  (sh "git", "worktree" "prune" :dir root))

(defn git-fetch [dir]
  "Runs git fetch in the directory"
  (sh "git" "fetch" :dir dir))

(defn git-clone [url dir]
  (= 0 (sh "git" "clone" url :dir dir )))

(defn git-create-worktree [root dir branch base-branch]
  "Runs work tree creating command and checks out the branch"

  (sh "git" "worktree" "add" "--detach" dir :dir root) :and-log (println "Git creating worktree in" dir "finished")
  (sh "git" "checkout" "--no-track" "-q" "-b" branch base-branch :dir dir) :and-log (println "Git checking out new branch" branch "finished")
  )

(defn git-add-worktree
  "Creates a work tree in dir then creates and checks out branch off of base-branch "
  ([root branch dir] (git-add-worktree root branch dir "origin/master"))
  ([root branch dir base-branch]
   (git-fetch root) :and-log (println "Git fetch finished")
   (git-prune-worktree root) :and-log (println "Git prun worktree finished")
   (sh "git" "checkout" base-branch :dir root) :and-log (println "Git checking out" base-branch "finished")
   (git-create-worktree root dir branch base-branch) :and-log (println "Worktree created!"))
  )

(defn -main [& args]
  (git-add-worktree "/Users/amanuel.mekonnen/auto1/refund-service/"
                    "master"
                    "/Users/amanuel.mekonnen/auto1/tickets/MOS-1234/refund-service/"
                    "master"
                    ))