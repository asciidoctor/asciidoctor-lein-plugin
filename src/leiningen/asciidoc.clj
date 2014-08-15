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
  (if (coll? elem) elem [elem]))

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


; Internal API : Configuration

(def ^:private DEF_FORMAT :html)
(def ^:private DEF_SOURCE_HIGHTLIGHTER "coderay")
(def ^:private DEF_SOURCES "src/asciidoc/*.asciidoc")

(defn- project-configs [prj] (scoll prj :asciidoc))
(defn- config-sources [conf] (scoll conf :sources [DEF_SOURCES]))
(defn- config-extract-css [conf] (sbool conf :extract-css))
(defn- config-to-dir [conf] (sget conf :to-dir))
(defn- config-format [conf] (name (sget conf :format DEF_FORMAT)))
(defn- config-compact [conf] (sbool conf :compact))
(defn- config-doctype [conf] (sget conf :doctype))
(defn- config-header-footer [conf] (sbool conf :header-footer true))

(defn- config-source-highlight [conf] (sbool conf :source-highlight))
(defn- config-no-footer [conf] (not (sbool conf :footer false)))
(defn- config-toc [conf] (sbool conf :toc false))
(defn- config-title [conf] (sget conf :title))

(defn- asciidoctor-attrs [conf]
  (let [attrs (HashMap.)]
    (if (config-source-highlight conf) (add-opt attrs :source-highlighter DEF_SOURCE_HIGHTLIGHTER))
    (if (config-no-footer conf) (add-opt attrs :nofooter true))
    (if (config-toc conf) (add-opt attrs :toc "toc"))
    (add-opt attrs :title (config-title conf))
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

(def ^:private RESOURCE_PATH "gems/asciidoctor-1.5.0.preview.7/data/stylesheets/")
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

(defn- process-source [asciidoctor pattern config]
  (let [sources (glob/glob pattern)
        options (asciidoctor-config config)]
    (doseq [source sources]
      (.convertFile asciidoctor source options)
      (if (config-extract-css config)
        (copy-resources source config))
      (log "Processed asciidoc file: %s" source))))

(defn- process-config [asciidoctor config]
  (let [sources (config-sources config)]
    (doseq [source sources]
      (process-source asciidoctor source config))))

(defn- proc [project & args]
  (let [asciidoctor (Asciidoctor$Factory/create)]
    (doseq [config (project-configs project)]
      (process-config asciidoctor config))
    (.shutdown asciidoctor)))


; External API: Leiningen tasks

(defn asciidoc
  "Generate documentation using Asciidoctor.

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
