(defproject lein-asciidoctor "0.1.14-SNAPSHOT"
  :description "A Leiningen plugin for generating documentation using Asciidoctor."
  :url "https://github.com/asciidoctor/asciidoctor-lein-plugin"
  :license {:name "The MIT License"
            :url "https://github.com/asciidoctor/asciidoctor-lein-plugin/blob/master/LICENSE.adoc"}

  :repositories [["lordofthejars" "http://dl.bintray.com/lordofthejars/maven"]]

  :dependencies [[clj-glob "1.0.0" :exclusions [org.clojure/clojure]]
                 [me.raynes/fs "1.4.6" :exclusions [org.clojure/clojure]]
                 [org.asciidoctor/asciidoctorj "1.5.2" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/slf4j-nop "1.7.10"]]

  :plugins [[jonase/eastwood "0.2.1" :exclusions [org.clojure/clojure]]
            [lein-kibit "0.0.8" :exclusions [org.clojure/clojure]]
            [lein-bikeshed "0.2.0" :exclusions [org.clojure/clojure]]
            [lein-ancient "0.6.3" :exclusions [org.clojure/clojure]]]

  :profiles {

    :dev {:dependencies [[midje "1.6.3" :exclusions [org.clojure/clojure joda-time]]]
          ; Don't use the latest version: https://github.com/marick/lein-midje/issues/47
          :plugins [[lein-midje "3.1.1"]]}

    :prod {:plugins [[lein-release "1.0.6" :exclusions [org.clojure/clojure]]]
           :global-vars {*warn-on-reflection* true}
           :lein-release {:deploy-via :clojars
                          :scm :git}}

  }

  :eval-in-leiningen true
  :pedantic? :abort
  :local-repo-classpath true)
