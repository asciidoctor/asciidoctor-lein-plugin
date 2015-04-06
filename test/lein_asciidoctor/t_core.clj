(ns ^{:author "Vladislav Bauer"}
  lein-asciidoctor.t-core
  (:use [midje.sweet :only [fact]]
        [midje.util :only [testable-privates]]
        [clojure.string :only [blank?]])
  (:require [lein-asciidoctor.core]
            [me.raynes.fs :as fs]))


; Configurations

(def ^:private DEF_INPUT "example/README.adoc")
(def ^:private DEF_OUTPUT "./test-out/")
(def ^:private DEF_OUTPUT_HTML "README.html")
(def ^:private DEF_OUTPUT_DOCBOOK "README.xml")

(testable-privates
  lein-asciidoctor.core
    log
    clean-path
    load-resource
    asciidoctor
    config-safe-mode
    RESOURCE_ASCIIDOCTOR
    RESOURCE_ASCIIDOCTOR_DEFAULT
    RESOURCE_CODERAY_ASCIIDOCTOR)


; Helper functions

(defn- config [fmt]
  {:asciidoctor
   {:sources DEF_INPUT
    :to-dir DEF_OUTPUT
    :format fmt
    :doctype :book
    :extract-css true
    :source-highlight true
    :toc :left}})

(defn- clean-output []
  (fs/delete-dir (clean-path DEF_OUTPUT)))

(defn- file-exists? [& parts]
  (let [path (clean-path (apply str parts))
        is-existed (fs/exists? path)
        is-file (fs/file? path)
        is-not-empty (> (fs/size path) 0)]
    (log "Path: %s (existed: %s, file: %s, not empty: %s)"
         path is-existed is-file is-not-empty)
    (and is-existed is-file is-not-empty)))

(defn- run-generator [out fmt]
  (try
    (do
      (clean-output)
      (asciidoctor (config fmt))
      (and
       (file-exists? DEF_OUTPUT out)
       (file-exists? DEF_OUTPUT (var-get RESOURCE_ASCIIDOCTOR))
       (file-exists? DEF_OUTPUT (var-get RESOURCE_CODERAY_ASCIIDOCTOR))))
    (finally (clean-output))))


; Tests

(fact "Check resources availability"
  (blank? (load-resource nil)) => true
  (blank? (load-resource (var-get RESOURCE_ASCIIDOCTOR_DEFAULT))) => false
  (blank? (load-resource (var-get RESOURCE_CODERAY_ASCIIDOCTOR))) => false)

(fact "Check documentation generator"
  (run-generator DEF_OUTPUT_HTML :html) => true
  (run-generator DEF_OUTPUT_HTML :html5) => true
  (run-generator DEF_OUTPUT_HTML :xhtml5) => true
  (run-generator DEF_OUTPUT_DOCBOOK :docbook) => true
  (run-generator DEF_OUTPUT_DOCBOOK :docbook5) => true
  (run-generator DEF_OUTPUT_DOCBOOK :docbook45) => true)

(fact "Check safe mode parameter"
  (config-safe-mode "1") => 1
  (config-safe-mode "2") => 2
  (config-safe-mode "safe") => 1
  (config-safe-mode "unknown") => 0
  (config-safe-mode nil) => 0)
