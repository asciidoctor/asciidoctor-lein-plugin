(ns ^{:author "Vladislav Bauer"}
    ^{:doc "Main entry point for plugin"}
  leiningen.asciidoctor
  (:require [lein-asciidoctor.core :as core]))


; External API: Leiningen tasks

(defn asciidoctor
  "Generate documentation using Asciidoctor.

  Configure :asciidoctor configuration parameter in the file project.clj using following options:
    :sources           - List of glob patterns to define input sources.
    :excludes          - List of glob patterns to prevent processing of some asciidoc files.
    :to-dir            - Target directory.
    :compact           - Remove blank lines. Possible values: true or false.
    :header-footer     - Suppress or allow the document header and footer in the output.
    :footer            - Suppress or allow the document footer in the output.
    :toc               - Add table of contents. Possible values: :auto, :left, :right.
    :toc-title         - Change title of the TOC.
    :toc-levels        - Set a deep of ToC levels. Possible values: 1, 2 (default), 3, 4, 5.
    :title             - Configure the title of document.
    :no-title          - Toggles the display of a document’s title.
    :format            - Backend output file format: :html, :html5, :docbook, :docbook45, :docbook5
    :doctype           - Document type: :article, :book, :manpage, :inline.
    :source-highlight  - Enable syntax hightlighter for source codes.
    :extract-css       - Extract CSS resources in the output directory.

  Usage:
    lein asciidoctor"

  [project & args]
  (core/asciidoctor project args))
