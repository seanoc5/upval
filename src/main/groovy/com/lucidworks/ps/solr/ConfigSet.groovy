package com.lucidworks.ps.solr

import groovy.json.JsonSlurper
import groovy.xml.XmlParser
import org.apache.log4j.Logger

import java.util.regex.Pattern

/**
 * wrapper class to help with solr schema parsing and operations
 */
class ConfigSet {
    Logger log = Logger.getLogger(this.class.name);
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
    }


    protected Integer populateParsedItems() {
        if (items) {
//            def schema = items.fin
            log.info "Parse schema"
            def schema = items['/managed-schema']
            if (schema) {
                managedSchema = new ManagedSchema(schema)
                log.info "Parsed new ManagedSchema, return code: $managedSchema"
            } else {
                log.warn "No schema file found in configset!!?"
            }
            solrConfig = parseSolrconfig(items)
            log.info "Parse solrconfig"

            log.info "Parse lang folder"
            def lf = populateLangFolder()

            log.info "Parse config overlay"
            def co = parseConfigOverlay()

            log.info "Parse stopwords"
            stopwords = items['/stopwords.txt']         // getting lazy, plus: stopwords are evil, don't use them!!

            log.info "Parse synonyms"
            def syn = parseSynonyms()

            log.info "Parse protwords"
            def pw = parseProtwords()

            log.info "done parsing configset: $this"
        } else {
            log.warn "We don't have 'items' yet, can't process...?"
        }
    }

    Node parseSchema(File src) {
        lines = sourceFile.readLines()
        def schema
        if (lines[0].contains('xml')) {
            log.info "File (${src} appears to be xml, parse with XMLParser (not xml slurper)"
            XmlParser parser = new XmlParser()
            xmlSchema = parser.parse(src)
            schema = xmlSchema
        } else if (lines[0].contains('{')) {
            log.info "File (${src} appears to be JSON, parse with JsonSlurper"
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

    Map<String, String> populateLangFolder(Pattern langMatch = ~/\/lang\/.+/) {
        langFolder = items.findAll { String path, def item ->
            path.contains('lang')
            path ==~ langMatch

        }
        return langFolder
    }

    def parseConfigOverlay(String coPath = '/configoverlay.json') {
        String co = items[coPath]
        JsonSlurper slurper = new JsonSlurper()
        configOverlay = slurper.parseText(co)
        log.debug "parsed configOverlay string: [$co] to object: $configOverlay"
        return configOverlay
    }

    /**
     * get the synonyms content
     * @todo - do we need to further process? probably not
     * @param synPath
     * @return string of content, unparsed
     */
    def parseSynonyms(String synPath = '/synonyms.txt') {
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
    def parseProtwords(String path = '/protwords.txt') {
        String pw = items[path]
    }
}
