(ns ^{:author "Vladislav Bauer"}
  leiningen.asciidoc
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

(defn- log [msg & args]
  (println (apply format msg args)))

(defn- to-coll [elem]
  (if (sequential? elem) elem [elem]))

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

(defn- sbool
  ([c k d] (boolean (sget c k d)))
  ([c k] (sbool c k false)))

(defn- scoll
  ([c k d] (to-coll (sget c k d)))
  ([c k] (scoll c k [])))

(defn- scan-files [patterns]
  (set (mapcat glob/glob patterns)))


; Internal API : Configuration

(def ^:private DEF_FORMAT :html)
(def ^:private DEF_SOURCE_HIGHTLIGHTER "coderay")
(def ^:private DEF_SOURCES "src/asciidoc/*.asciidoc")

(defn- project-configs [prj] (scoll prj :asciidoc))
(defn- config-sources [conf] (scoll conf :sources [DEF_SOURCES]))
(defn- config-excludes [conf] (scoll conf :excludes))
(defn- config-extract-css [conf] (sbool conf :extract-css))
(defn- config-to-dir [conf] (sget conf :to-dir))
(defn- config-format [conf] (name (sget conf :format DEF_FORMAT)))
(defn- config-compact [conf] (sbool conf :compact))
(defn- config-doctype [conf] (sget conf :doctype))
(defn- config-header-footer [conf] (sbool conf :header-footer true))

(defn- config-source-highlight [conf] (sbool conf :source-highlight))
(defn- config-no-header [conf] (not (sbool conf :header true)))
(defn- config-no-footer [conf] (not (sbool conf :footer false)))
(defn- config-toc [conf] (sget conf :toc))
(defn- config-toc-title [conf] (sget conf :toc-title))
(defn- config-toc-levels [conf] (sget conf :toc-levels))
(defn- config-title [conf] (sget conf :title))
(defn- config-no-title [conf] (sbool conf :no-title false))

(defn- asciidoctor-attrs [conf]
  (let [attrs (HashMap.)]
    (if (config-source-highlight conf) (add-opt attrs :source-highlighter DEF_SOURCE_HIGHTLIGHTER))
    (if (config-no-header conf) (add-opt attrs :noheader true))
    (if (config-no-footer conf) (add-opt attrs :nofooter true))
    (if (config-no-title conf) (add-opt attrs :notitle true))
    (doto attrs
      (add-opt :toc (config-toc conf))
      (add-opt :title (config-title conf))
      (add-opt :toc-title (config-toc-title conf))
      (add-opt :toclevels (config-toc-levels conf)))
    attrs))

(defn- asciidoctor-config [conf]
  (let [config (HashMap.)]
    (if-let [to-dir (config-to-dir conf)]
      (do
        (fs/mkdirs to-dir)
        (add-opt config :to_dir to-dir)))
    (doto config
      (add-opt :attributes (asciidoctor-attrs conf))
      (add-opt :header_footer (config-header-footer conf))
      (add-opt :doctype (config-doctype conf))
      (add-opt :compact (config-compact conf))
      (add-opt :backend (config-format conf)))))


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
  (let [dir (or (config-to-dir options) (fs/parent source))]
    (copy-resource RESOURCE_ASCIIDOCTOR_DEFAULT dir RESOURCE_ASCIIDOCTOR)
    (if (config-source-highlight options)
      (copy-resource RESOURCE_CODERAY_ASCIIDOCTOR dir RESOURCE_CODERAY_ASCIIDOCTOR))))


; Internal API : Renderer / Processor

(defn- source-list [config]
  (let [sources (scan-files (config-sources config))
        excludes (scan-files (config-excludes config))]
    (remove (fn [s] (some #(.compareTo % s) excludes)) sources)))

(defn- process-source [asciidoctor source config]
  (let [options (asciidoctor-config config)]
    (.convertFile asciidoctor source options)
    (if (config-extract-css config)
      (copy-resources source config))
    (log "Processed asciidoc file: %s" source)))

(defn- process-config [asciidoctor config]
  (let [sources (source-list config)]
    (doseq [source sources]
      (process-source asciidoctor source config))))

(defn- proc [project & args]
  (let [asciidoctor (Asciidoctor$Factory/create)
        configs (project-configs project)]
    (doseq [config configs]
      (process-config asciidoctor config))
    (.shutdown asciidoctor)))


; External API: Leiningen tasks

(defn asciidoc
  "Generate documentation using Asciidoctor.

  Configure :asciidoc configuration parameter in the file project.clj using following options:
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
    lein asciidoc"

  [project & args]
  (proc project args))


; External API: Leiningen hooks

(defn gen-hook [f & args]
  (let [res (apply f args)]
    (proc (first args))
    res))

(defn activate []
  (hooke/add-hook #'leiningen.compile/compile #'gen-hook))
