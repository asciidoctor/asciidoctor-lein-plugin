(defproject example "0.1.0-SNAPSHOT"
  :description "Simple example of using lein-asciidoctor"
  :url "https://github.com/asciidoctor/asciidoctor-lein-plugin/tree/master/example"
  :license {:name "The MIT License"
            :url "https://github.com/asciidoctor/asciidoctor-lein-plugin/blob/master/LICENSE.adoc"}

  :plugins [[lein-asciidoctor "0.1.8"]]

  :asciidoc {:sources "README.adoc"
             :to-dir "generated"
             :extract-css true
             :source-highlight true
             :toc :left})
