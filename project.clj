(defproject lein-asciidoctor "0.1.15"
  :description "A Leiningen plugin for generating documentation using Asciidoctor."
  :url "https://github.com/asciidoctor/asciidoctor-lein-plugin"
  :license {:name "The MIT License"
            :url "https://github.com/asciidoctor/asciidoctor-lein-plugin/blob/master/LICENSE.adoc"}

  :dependencies [[me.raynes/fs "1.4.6" :exclusions [org.clojure/clojure]]
                 [org.asciidoctor/asciidoctorj "1.5.4.1" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/slf4j-nop "1.7.22"]]

  :profiles {

    :dev {:dependencies [[midje "1.6.3" :exclusions [org.clojure/clojure joda-time]]]
          ; Don't use the latest version: https://github.com/marick/lein-midje/issues/47
          :plugins [[lein-midje "3.1.1"]]}

  }

  :eval-in-leiningen true
  :pedantic? :abort
  :local-repo-classpath true)
