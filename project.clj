(defproject lein-asciidoctor "0.1.8-SNAPSHOT"
  :description "A Leiningen plugin for generating documentation using Asciidoctor."
  :url "https://github.com/asciidoctor/asciidoctor-lein-plugin"
  :license {:name "The MIT License"
            :url "https://github.com/asciidoctor/asciidoctor-lein-plugin/blob/master/LICENSE.adoc"}

  :repositories [["lordofthejars" "http://dl.bintray.com/lordofthejars/maven"]]

  :dependencies [[clj-glob "1.0.0" :exclusions [org.clojure/clojure]]
                 [me.raynes/fs "1.4.6" :exclusions [org.clojure/clojure]]
                 [org.asciidoctor/asciidoctorj "1.5.0" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/slf4j-nop "1.7.7"]]

  :plugins [[jonase/eastwood "0.1.4" :exclusions [org.clojure/clojure]]
            [lein-release "1.0.5" :exclusions [org.clojure/clojure]]
            [lein-kibit "0.0.8" :exclusions [org.clojure/clojure]]
            [lein-bikeshed "0.1.7" :exclusions [org.clojure/clojure]]
            [lein-ancient "0.5.5"]]

  :eval-in-leiningen true
  :pedantic? :abort
  :global-vars {*warn-on-reflection* true}

  :local-repo-classpath true
  :lein-release {:deploy-via :clojars
                 :scm :git})
