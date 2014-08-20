(defproject example "0.1.0-SNAPSHOT"
  :description "Simple example of using lein-asciidoc"
  :url "https://github.com/vbauer/lein-asciidoc"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-asciidoc "0.1.7"]]

  :asciidoc {:sources "README.asciidoc"
             :to-dir "generated"
             :extract-css true
             :source-highlight true
             :toc :left})
