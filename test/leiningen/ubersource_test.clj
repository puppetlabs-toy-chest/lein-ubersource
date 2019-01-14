(ns leiningen.ubersource-test
  (:require [clojure.test :refer :all]
            [leiningen.ubersource :refer :all]
            [me.raynes.fs :as fs]))

(def sample-direct-deps
  [['org.clojure/clojure "1.6.0"]
   ['puppetlabs/certificate-authority "0.1.4"]
   ['me.raynes/fs "1.4.5"]])

(def sample-transitive-deps
  #{['org.clojure/clojure "1.6.0"]
    ['puppetlabs/certificate-authority "0.1.4"]
    ['me.raynes/fs "1.4.5"]
    ['clj-time "0.5.1"]
    ['joda-time "2.2"]
    ['org.apache.commons/commons-compress "1.4"]
    ['org.tukaani/xz "1.0"]
    ['org.bouncycastle/bcpkix-jdk15on "1.50"]
    ['org.bouncycastle/bcprov-jdk15on "1.50"]
    ['org.clojure/tools.logging "0.2.6"]})

(defn delete-on-exit
  "Will delete `f` on shutdown of the JVM"
  [f]
  (.deleteOnExit (fs/file f))
  f)

(defn temp-dir
  "Creates a temporary directory that will be deleted on JVM shutdown.

  Supported arguments are the same as for me.raynes.fs/temp-dir:
  [prefix]
  [prefix suffix]
  [prefix suffix tries]

  You may also call with no arguments, in which case the prefix string will be
  empty."
  [& args]
  temp-dir
  (if (empty? args)
    (delete-on-exit (fs/temp-dir nil))
    (delete-on-exit (apply fs/temp-dir args))))

(def repositories
  [["central" {:snapshots false, :url "https://repo1.maven.org/maven2/"}]
   ["clojars" {:url "https://clojars.org/repo/"}]])

(deftest ubersource-test
  (testing "Can find all dependencies for a project (including transitive)"
    (let [deps (find-transitive-deps
                 sample-transitive-deps
                 repositories)]
      (mapv println deps)
      (is (= sample-transitive-deps deps))))

  (testing "Can resolve the sources jar for a dependency if there is one"
    (let [d (temp-dir)]
      (download-source-for-dep
        ['org.apache.commons/commons-compress "1.4"]
        repositories
        d)
      (is (fs/directory? (fs/file d "commons-compress" "1.4" "org")))))

  (testing "Falls back to the main jar for a dependency that doesn't have a source jar"
    (let [d (temp-dir)]
      (download-source-for-dep
        ['me.raynes/fs "1.4.5"]
        repositories
        d)
      (is (fs/file? (fs/file d "fs" "1.4.5" "me" "raynes" "fs.clj")))))

  (testing "Downloads all sources for a project"
    (let [d       (temp-dir)
          project {:repositories repositories
                   :dependencies sample-transitive-deps
                   :target-path d}]
      (ubersource project)
      (doseq [dep sample-transitive-deps]
        (is (fs/directory? (fs/file d "ubersource" (name (first dep)) (second dep))))))))
