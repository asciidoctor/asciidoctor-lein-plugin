(ns ^{:author "Vladislav Bauer"}
  leiningen.asciidoctor
  (:import (org.asciidoctor Asciidoctor$Factory Options Attributes)
           (java.util HashMap))
  (:require [leiningen.core.main :as main]
            [leiningen.compile]
            [robert.hooke :as hooke]
            [org.satta.glob :as glob]
            [me.raynes.fs :as fs]
            [clojure.walk :as walk]
            [clojure.string :as string]
            [clojure.java.io :as io]))


; Internal API: Common

(defn- scan-files [patterns] (set (mapcat glob/glob patterns)))
(defn- log [msg & args] (println (apply format msg args)))
(defn- find-first [coll f] (first (filter f coll)))
(defn- to-coll [e] (if (nil? e) [] (if (sequential? e) e [e])))

(defn- add-opt [c k v]
  (if (not (nil? v))
    (.put c (name k)
          (if (keyword? v) (name v) v))))

(defn- sget
  ([c k]
   (let [v (get c k)]
     (if (nil? v)
       (if (keyword? k)
         (get c (name k))
         (get c (keyword k)))
       v)))
  ([c k v]
   (let [r (sget c k)]
     (if (nil? r) v r))))

(defn- sname [c k d]
  (let [r (sget c k d)]
    (when-not (nil? r) (name r))))

(defn- sbool [c k d] (boolean (sget c k d)))
(defn- scoll [c k d] (to-coll (sget c k d)))


; Internal API : Configuration

; Format of the transformation table for reading configuration:
; Parameter name   Converter   [Default value]
(def ^:private DEF_CONF
  [; Root configuration
   [:asciidoctor        scoll                            ]

   ; Configuration options
   [:sources            scoll      "src/asciidoc/*.adoc" ]
   [:excludes           scoll                            ]
   [:extract-css        sbool                            ]
   [:to-dir             sget                             ]
   [:format             sname      :html                 ]
   [:compact            sbool                            ]
   [:doctype            sname                            ]
   [:header-footer      sbool      true                  ]

   ; Configuration attributes
   [:source-highlight   sbool                            ]
   [:toc                sget                             ]
   [:toc-title          sget                             ]
   [:toc-levels         sget                             ]
   [:title              sget                             ]
   [:no-title           sbool      false                 ]
   [:no-header          sbool      false                 ]
   [:no-footer          sbool      true                  ]])


(defn- config [conf k]
  (let [scanner (fn [e] (= k (nth e 0)))
        params (find-first DEF_CONF scanner)
        func (nth params 1)
        defarg (nth params 2 nil)]
    (func conf k defarg)))

(defn- asciidoctor-attrs [conf]
  (let [attrs (HashMap.)]
    (if (config conf :source-highlight) (add-opt attrs :source-highlighter "coderay"))
    (if (config conf :no-header) (add-opt attrs :noheader true))
    (if (config conf :no-footer) (add-opt attrs :nofooter true))
    (if (config conf :no-title) (add-opt attrs :notitle true))
    (doto attrs
      (add-opt :toc (config conf :toc))
      (add-opt :title (config conf :title))
      (add-opt :toc-title (config conf :toc-title))
      (add-opt :toclevels (config conf :toc-levels)))
    attrs))

(defn- asciidoctor-config [conf]
  (let [result (HashMap.)]
    (if-let [to-dir (config conf :to-dir)]
      (do
        (fs/mkdirs to-dir)
        (add-opt result :to_dir to-dir)))
    (doto result
      (add-opt :attributes (asciidoctor-attrs conf))
      (add-opt :header_footer (config conf :header-footer))
      (add-opt :doctype (config conf :doctype))
      (add-opt :compact (config conf :compact))
      (add-opt :backend (config conf :format)))))


; Internal API : Resources

(def ^:private RESOURCE_PATH "gems/asciidoctor-1.5.0/data/stylesheets/")
(def ^:private RESOURCE_ASCIIDOCTOR "asciidoctor.css")
(def ^:private RESOURCE_ASCIIDOCTOR_DEFAULT "asciidoctor-default.css")
(def ^:private RESOURCE_CODERAY_ASCIIDOCTOR "coderay-asciidoctor.css")


(defn- write-file [file content]
  (with-open [writer (io/writer file)]
    (.write writer content)))

(defn- copy-resource [in dir file]
  (let [outf (io/file dir file)]
    (if (not (.exists outf))
      (let [res (io/resource (str RESOURCE_PATH in))]
        (write-file
         (str outf)
         (slurp res))))))

(defn- copy-resources [source options]
  (let [dir (or (config options :to-dir) (fs/parent source))]
    (copy-resource RESOURCE_ASCIIDOCTOR_DEFAULT dir RESOURCE_ASCIIDOCTOR)
    (if (config options :source-highlight)
      (copy-resource RESOURCE_CODERAY_ASCIIDOCTOR dir RESOURCE_CODERAY_ASCIIDOCTOR))))


; Internal API : Renderer / Processor

(defn- source-list [conf]
  (let [sources (scan-files (config conf :sources))
        excludes (scan-files (config conf :excludes))]
    (remove (fn [s] (some #(.compareTo % s) excludes)) sources)))

(defn- process-source [engine source conf]
  (let [options (asciidoctor-config conf)]
    (.convertFile engine source options)
    (if (config conf :extract-css)
      (copy-resources source conf))
    (log "Processed asciidoc file: %s" source)))

(defn- process-config [engine conf]
  (let [sources (source-list conf)]
    (doseq [source sources]
      (process-source engine source conf))))

(defn- proc [project & args]
  (let [engine (Asciidoctor$Factory/create)
        configs (config project :asciidoctor)]
    (doseq [conf configs]
      (process-config engine conf))
    (.shutdown engine)))


; External API: Leiningen tasks

(defn asciidoctor
  "Generate documentation using Asciidoctor.

  Configure :asciidoctor configuration parameter in the file project.clj using following options:
    :sources           - List of glob patterns to define input sources.
    :excludes          - List of glob patterns to prevent processing of some asciidoc files.
    :to-dir            - Target directory.
    :compact           - Remove blank lines.
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
  (proc project args))


; External API: Leiningen hooks

(defn gen-hook [f & args]
  (let [res (apply f args)]
    (proc (first args))
    res))

(defn activate []
  (hooke/add-hook #'leiningen.compile/compile #'gen-hook))
