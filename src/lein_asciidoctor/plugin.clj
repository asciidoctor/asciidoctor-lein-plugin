(ns ^{:author "Vladislav Bauer"}
    ^{:doc "Extra Leiningen hooks"}
  lein-asciidoctor.plugin
  (:require [leiningen.compile]
            [robert.hooke :as hooke]
            [lein-asciidoctor.core :as core]))


; External API: Leiningen hooks

(defn compile-hook [task project & args]
  (let [res (apply task project args)]
    (core/asciidoctor project args)
    res))

(defn activate []
  (hooke/add-hook #'leiningen.compile/compile #'compile-hook))
