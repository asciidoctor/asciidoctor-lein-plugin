(defproject lein-asciidoctor "0.1.16"
  :description "A Leiningen plugin for generating documentation using Asciidoctor."
  :url "https://github.com/asciidoctor/asciidoctor-lein-plugin"
  :license {:name "The MIT License"
            :url "https://github.com/asciidoctor/asciidoctor-lein-plugin/blob/master/LICENSE.adoc"}

  :dependencies [[me.raynes/fs "1.4.6" :exclusions [org.clojure/clojure]]
                 [org.asciidoctor/asciidoctorj "1.5.8.1"]]

  :repositories [[ "clojars" {:sign-releases false} ]]

  :eval-in-leiningen true
  :pedantic? :abort
  :local-repo-classpath true)
