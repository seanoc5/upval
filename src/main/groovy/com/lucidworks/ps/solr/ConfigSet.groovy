package com.lucidworks.ps.solr

import groovy.json.JsonSlurper
import groovy.xml.XmlParser
import org.apache.log4j.Logger

import java.util.regex.Pattern

/**
 * wrapper class to help with solr schema parsing and operations
 */
class ConfigSet {
    public static final Pattern SOLR_CONFIG_PATTERN = '/solrconfig.xml'
    Logger log = Logger.getLogger(this.class.name);
    public static final Pattern LANG_FOLDER_PATTERN = ~/\/lang\/.+/
    String configsetName
    Map<String, String> items

    ManagedSchema managedSchema
    SolrConfig solrConfig

    Map<String, String> langFolder
    def configOverlay
    def stopwords = ''
    def synonyms = ''
    def protwords = ''

    ConfigSet(String configsetName, Map<String, Object> items) {
        this.configsetName = configsetName
        this.items = items
        def foo = populateParsedItems()
        log.info "Constructor(name, items)-> ${this.toString()}"
    }


    protected Integer populateParsedItems() {
        if (items) {
//            def schema = items.fin
            log.debug "Parse schema"
            def schema = items['/managed-schema']
            if (schema) {
                managedSchema = new ManagedSchema(schema, this.configsetName)
                log.debug "Parsed new ManagedSchema, return code: $managedSchema"
            } else {
                log.warn "No schema file found in configset!!?"
            }
            solrConfig = parseSolrconfig(items)
            log.debug "Parse solrconfig"

            log.debug "Parse lang folder"
            langFolder = populateLangFolder(items)
            langFolder = populateLangFolder(LANG_FOLDER_PATTERN)

            log.debug "Parse config overlay"
            configOverlay = parseConfigOverlay(items)
            configOverlay = parseConfigOverlay()

            log.debug "Parse stopwords"
            stopwords = parseStopwords(items)         // getting lazy, plus: stopwords are evil, don't use them!!

            log.debug "Parse synonyms"
            synonyms = parseSynonyms(items)

            log.debug "Parse protwords"
            protwords = parseProtwords(items)

            log.debug "done parsing configset: $this"
        } else {
            log.warn "We don't have 'items' yet, can't process...?"
        }
    }

    /**
     * one approach to parsing the schema -- probably not the one you want...?
     * @deprecated @see ManagedSchema
     * @param src file to parse
     * @return the node for groovy gpath xml processing
     */
    Node parseSchema(File src) {
        lines = sourceFile.readLines()
        def schema
        if (lines[0].contains('xml')) {
            log.info "File (${src} appears to be xml, parse with XMLParser (not xml slurper)"
            XmlParser parser = new XmlParser()
            xmlSchema = parser.parse(src)
            schema = xmlSchema
        } else if (lines[0].contains('{')) {
            log.warn "File (${src} appears to be JSON, parse with JsonSlurper -- is this FULLY SUPPORTED NOW?"
            JsonSlurper slurper = new JsonSlurper()
            schemaMap = slurper.parse(src)
            schema = schemaMap
        }
        return schema
    }

    SolrConfig parseSolrconfig(Map<String, Object> items) {
        String scXml = items['/solrconfig.xml']
        SolrConfig solrConfig = scXml ? new SolrConfig(scXml) : null
        return solrConfig
    }


    /**
     * run through the 'tree' of zk nodes, and gather those entries for Solr Language settings
     * @param items
     * @param langMatch
     * @return
     */
    Map<String, String> populateLangFolder(Map items, LANG_FOLDER_PATTERN) {
        Map langFolder = items.findAll { String path, def item ->
            path.contains('lang')
            path ==~ langMatch

        }
        return langFolder
    }


    /**
     * parse the json (assumption!) in the configOverlay string
     * @note no actual use yet (2022-June)
     * @param items
     * @param coPath
     * @return map of parsed json
     */

    /**
     * parse the json (assumption!) in the configOverlay string
     * @note no actual use yet (2022-June)
     * @param items
     * @param coPath
     * @return map of parsed json
     */
    def parseConfigOverlay(Map items, String coPath = '/configoverlay.json') {
        String co = items[coPath]
        Map coMap = null
        if(co) {
            JsonSlurper slurper = new JsonSlurper()
            coMap = slurper.parseText(co)
            log.debug "parsed configOverlay string: [$co] to object: $configOverlay"
        }
        return coMap
    }

    /**
     * get the synonyms content
     * @todo - do we need to further process? probably not
     * @param synPath
     * @return string of content, unparsed
     */
    def parseSynonyms(Map items, String synPath = '/synonyms.txt') {
        String syns = items[synPath]
        // todo consider:
        // List<String> synlines = syns.split('\n')
        // return [oneway:[synlines.findAll{it.contains('=')}, twoway:synlines.findAll{!it.contains('=')}]
        synonyms = syns
    }

    /**
     * return unparsed protected words...
     * todo - more processing valuable here??
     * @param path
     */
    def parseProtwords(Map items, String path = '/protwords.txt') {
        String pw = items[path]
    }

    String parseStopwords(Map<String, String> items) {
        items['/stopwords.txt']
    }

    String toString() {
        String s = "Configset ($configsetName): Schema:(${managedSchema.toString()}) SolrConfig:(${solrConfig.toString()}) ... "
    }
}
