(defproject github.amexboy "0.1.0-SNAPSHOT"
  :description "Intellij Project generator"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/tools.cli "0.4.2"]
                 [io.reactivex.rxjava2/rxjava "2.2.8"]]

  :repl-options {:init-ns github.amexboy.core}
  :main github.amexboy.core)
