(ns ^{:author "Vladislav Bauer"}
  lein-asciidoctor.t-core
  (:use [midje.sweet]
        [midje.util :only [testable-privates]]
        [clojure.string :only [blank?]])
  (:require [lein-asciidoctor.core]))


; Configurations

(testable-privates
  lein-asciidoctor.core
    load-resource
    RESOURCE_ASCIIDOCTOR_DEFAULT
    RESOURCE_CODERAY_ASCIIDOCTOR)


; Tests

(fact "Check resources availability"
  (blank? (load-resource nil)) => true
  (blank? (load-resource (var-get RESOURCE_ASCIIDOCTOR_DEFAULT))) => false
  (blank? (load-resource (var-get RESOURCE_CODERAY_ASCIIDOCTOR))) => false)
