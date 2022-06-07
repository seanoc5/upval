package com.lucidworks.ps

import groovy.json.JsonSlurper
import groovy.xml.XmlParser
import org.apache.log4j.Logger

/**
 * wrapper class to help with solr schema parsing and operations
 */
class SolrSchema {
    Logger log = Logger.getLogger(this.class.name);
    File sourceFile
    List<String> lines
    Map schemaMap = [:]
    Node xmlSchema = null
    def fieldTypes
    def definedFields

    SolrSchema(File src) {
        sourceFile = src
        parseSchema(src)
    }
/*    SolrSchema(File src, File lukeOutput) {
        sourceFile = src
        parseSource(src)
    }*/

    def parseSchema(File src) {
        lines = sourceFile.readLines()
        def schema
        if (lines[0].contains('xml')) {
            log.info "File (${src} appears to be xml, parse with XMLParser (not xml slurper)"
            XmlParser parser = new XmlParser()
            xmlSchema = parser.parse(schemaSource)
            schema = xmlSchema
        } else if (lines[0].contains('{')) {
            log.info "File (${src} appears to be JSON, parse with JsonSlurper"
            JsonSlurper slurper = new JsonSlurper()
            schemaMap = slurper.parse(src)
            schema = schemaMap
        }
        return schema
    }

    /**
     * todo -- rework this code
     * @return
     */
    def foo() {
        fieldTypes = schema.'**'.findAll { Node node ->
            node.name() == 'fieldType'
        }
        definedFields = schema.'**'.findAll { Node node ->
            node.name() == 'field'
        }

    }
}
