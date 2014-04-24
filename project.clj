(defproject lein-ubersource "0.1.0-SNAPSHOT"
  :description "A leiningen plugin that attempts to download the source code for all of a project's (transitive) dependencies"
  :eval-in-leiningen true
  :pedantic? abort

  :dependencies [[me.raynes/fs "1.4.5" :exclusions [org.clojure/clojure]]]

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.5.1"]
                                  [com.cemerick/pomegranate "0.2.0" :exclusions [org.codehaus.plexus/plexus-utils]]
                                  [org.codehaus.plexus/plexus-utils "3.0"]]}})
