(ns ^{:author "Vladislav Bauer"}
  lein-asciidoctor.t-core
  (:require [lein-asciidoctor.core :as a]
            [me.raynes.fs :as fs]
            [clojure.string :as s]
            [clojure.test :as t]))


; Configurations

(def ^:private DEF_INPUT "example/README.adoc")
(def ^:private DEF_OUTPUT "./test-out/")
(def ^:private DEF_OUTPUT_HTML "README.html")
(def ^:private DEF_OUTPUT_DOCBOOK "README.xml")


; Helper functions

(defn- config [fmt]
  {:asciidoctor
   {:sources DEF_INPUT
    :excludes []
    :to-dir DEF_OUTPUT
    :format fmt
    :doctype :book
    :extract-css true
    :source-highlight true
    :compact false
    :header-footer true
    :no-title false
    :toc :left}})

(defn- clean-output []
  (fs/delete-dir DEF_OUTPUT))

(defn- file-exists? [& parts]
  (let [path (apply str parts)
        is-existed (fs/exists? path)
        is-file (fs/file? path)
        is-not-empty (> (fs/size path) 0)]
    (and is-existed is-file is-not-empty)))

(defn- run-generator [out fmt]
  (try
    (do
      (clean-output)
      (a/asciidoctor (config fmt))
      (and
       (file-exists? DEF_OUTPUT out)
       (file-exists? DEF_OUTPUT a/RESOURCE_ASCIIDOCTOR)
       (file-exists? DEF_OUTPUT a/RESOURCE_CODERAY_ASCIIDOCTOR)))
    (finally (clean-output))))


; Tests

(t/deftest check-documentation-generator
  (t/is (run-generator DEF_OUTPUT_HTML :html))
  (t/is (run-generator DEF_OUTPUT_HTML :html5))
  (t/is (run-generator DEF_OUTPUT_HTML :xhtml5))
  (t/is (run-generator DEF_OUTPUT_DOCBOOK :docbook))
  (t/is (run-generator DEF_OUTPUT_DOCBOOK :docbook5))
  (t/is (run-generator DEF_OUTPUT_DOCBOOK :docbook45)))

