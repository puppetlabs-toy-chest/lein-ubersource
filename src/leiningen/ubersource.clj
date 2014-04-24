(ns leiningen.ubersource
  (:import (org.sonatype.aether.resolution DependencyResolutionException)
           (java.util.zip ZipFile))
  (:require [clojure.pprint :refer [pprint]]
            [cemerick.pomegranate.aether :as aether]
            [me.raynes.fs :as fs]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn zip-target-file
  [target-dir entry-path]
  ;; remove leading slash in case some bonehead created a zip with absolute
  ;; file paths in it.
  (let [entry-path (str/replace-first (str entry-path) #"^/" "")]
    (fs/file target-dir entry-path)))

(defn unzip
  "Takes the path to a zipfile source and unzips it to target-dir."
  ([source]
   (unzip source (name source)))
  ([source target-dir]
   (let [zip (ZipFile. (fs/file source))
         entries (enumeration-seq (.entries zip))]
     (doseq [entry entries :when (not (.isDirectory ^java.util.zip.ZipEntry entry))
             :let [f (zip-target-file target-dir entry)]]
       (fs/mkdirs (fs/parent f))
       (io/copy (.getInputStream zip entry) f)))
   target-dir))


(defn find-transitive-deps
  [deps repositories]
  (->> (aether/resolve-dependencies
        :coordinates deps
        :repositories repositories)
      keys
      set))

(defn resolve-artifact
  [dep repositories]
  (->> (find-transitive-deps [dep] repositories)
      (filter #(= % dep))
      first))

(defn try-resolve-sources-artifact!
  [dep repositories]
  (try
    (println "Looking for source jar for " dep)
    (resolve-artifact (concat dep [:classifier "sources"]) repositories)
    (catch DependencyResolutionException ex
      nil)))

(defn main-artifact
  [dep repositories]
  (println "Falling back to main jar for " dep)
  (resolve-artifact dep repositories))

(defn source-jar-or-main-jar
  [dep repositories]
  (if-let [art (try-resolve-sources-artifact! dep repositories)]
    art
    (main-artifact dep repositories)))

(defn download-source-for-dep
  [dep repositories target-path]
  (let [art (source-jar-or-main-jar dep repositories)
        source-jar (-> art meta :file)
        artifact-name (name (first dep))
        artifact-version (second dep)
        target (fs/file target-path artifact-name artifact-version)]
    (if (fs/exists? target)
      (println "Source directory already found; skipping: " target)
      (do
        (fs/mkdirs target)
        (println (format "Unzipping '%s' to '%s'" source-jar target))
        (unzip source-jar target)))))

(defn ubersource
  "I don't do a lot."
  [{:keys [repositories dependencies target-path] :as project} & args]
  (doseq [dep (find-transitive-deps dependencies repositories)]
    (download-source-for-dep
      dep
      (:repositories project)
      (fs/file (:target-path project) "ubersource"))))
