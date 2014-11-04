(ns ^{:author "Vladislav Bauer"}
  lein-asciidoctor.t-core
  (:use [midje.sweet :only [fact]]
        [midje.util :only [testable-privates]]
        [clojure.string :only [blank?]])
  (:require [lein-asciidoctor.core]
            [me.raynes.fs :as fs]))


; Configurations

(def ^:private DEF_OUTPUT "./test-out/")
(def ^:private DEF_CONFIG
  {:asciidoctor {:sources "example/README.adoc"
                 :to-dir DEF_OUTPUT
                 :extract-css true
                 :source-highlight true
                 :toc :left}})

(testable-privates
  lein-asciidoctor.core
    load-resource
    asciidoctor
    RESOURCE_ASCIIDOCTOR
    RESOURCE_ASCIIDOCTOR_DEFAULT
    RESOURCE_CODERAY_ASCIIDOCTOR)


; Helper functions

(defn- clean-output []
  (fs/delete-dir DEF_OUTPUT))

(defn- run-generator []
  (try
    (do
      (clean-output)
      (asciidoctor DEF_CONFIG)
      (and
       (fs/exists? (str DEF_OUTPUT "README.html"))
       (fs/exists? (str DEF_OUTPUT (var-get RESOURCE_ASCIIDOCTOR)))
       (fs/exists? (str DEF_OUTPUT (var-get RESOURCE_CODERAY_ASCIIDOCTOR)))))
    (finally (clean-output))))


; Tests

(fact "Check resources availability"
  (blank? (load-resource nil)) => true
  (blank? (load-resource (var-get RESOURCE_ASCIIDOCTOR_DEFAULT))) => false
  (blank? (load-resource (var-get RESOURCE_CODERAY_ASCIIDOCTOR))) => false)

(fact "Check documentation generator"
  (run-generator) => true)
