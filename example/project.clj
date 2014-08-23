(defproject example "0.1.0-SNAPSHOT"
  :description "Simple example of using lein-asciidoc"
  :url "https://github.com/vbauer/lein-asciidoc"
  :license {:name "The MIT License"
            :url "https://github.com/asciidoctor/asciidoctor-lein-plugin/blob/master/LICENSE.adoc"}

  :plugins [[lein-asciidoc "0.1.7"]]

  :asciidoc {:sources "README.adoc"
             :to-dir "generated"
             :extract-css true
             :source-highlight true
             :toc :left})
