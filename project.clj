(defproject lein-asciidoc "0.1.3-SNAPSHOT"
  :description "A Leiningen plugin for generating documentation using Asciidoctor."
  :url "https://github.com/vbauer/lein-asciidoc"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :repositories [["lordofthejars" "http://dl.bintray.com/lordofthejars/maven"]]

  :dependencies [[clj-glob "1.0.0" :exclusions [org.clojure/clojure]]
                 [me.raynes/fs "1.4.6" :exclusions [org.clojure/clojure]]
                 [org.asciidoctor/asciidoctorj "1.5.0.preview.7" :exclusions [org.slf4j/slf4j-api]]
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
