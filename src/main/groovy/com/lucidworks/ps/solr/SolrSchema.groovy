package com.lucidworks.ps.solr

import groovy.json.JsonSlurper
import groovy.xml.XmlParser
import org.apache.log4j.Logger

import java.util.regex.Pattern

/**
 * wrapper class to help with solr schema parsing and operations
 */
class SolrSchema {
    public static final Pattern OVERRIDE_FIELDNAMES = ~/id|_version_|_raw_content_|_root_/
    Logger log = Logger.getLogger(this.class.name);
    File sourceFile
    List<String> lines
    Map schemaMap = [:]
    Node xmlSchema = null
    List<Node> fieldTypes
    List<Node> definedFields
    Map<String, Map> lukeMap
    Map<String, Map> lukeFields

    SolrSchema(File src) {
        sourceFile = src
        parseSchema(src)
    }

    SolrSchema(String src) {
        lines = src.split('\n')
        parseSchema(src)
    }

    SolrSchema(URL srcUrl) {
        sourceFile = new File(srcUrl.toURI())
        parseSchema(sourceFile)
        fieldTypes = collectFieldTypes()
        definedFields = collectFields()
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

    /**
     * parse luke output and include parsed field information
     * this can help verify what (dynamic) fields are in the index, but not used (0 docs)
     * @param lukeSource json export/file from solr admin luke request
     * @return the parsed list of fields from luke
     */
    def parseLukeOutput(def lukeSource) {
        JsonSlurper slurper = new JsonSlurper()
        lukeMap = slurper.parse(lukeSource)
        lukeFields = lukeMap.fields
    }


    /**
     * use luke output to see what fields are actually in use
     * @param overrideAsUsed -- pattern to keep matching fields as 'used' regardless of luke information
     * @return
     */
    def findUsedFieldsLuke(Pattern overrideAsUsed = OVERRIDE_FIELDNAMES) {
        def usedFields = [:]
        Map<String, Map<String, Object>> fieldNames = [:].withDefault { [:] }
        lukeFields.each { String fieldName, def lukeFieldInfo ->
            if (lukeFieldInfo.docs > 0) {
                fieldNames[fieldName].luke = lukeFieldInfo
            } else if (fieldName ==~ overrideAsUsed) {
                log.info "adding special field: $fieldName even though luke says there are no docs..."
                fieldNames[fieldName].luke = lukeFieldInfo
            } else {
                log.info "\t\tLuke says field: $fieldName has no documents, consider it defined but not used"
            }
        }
        return fieldNames
    }

    def findUnusedFields(Pattern overrideAsUsed = OVERRIDE_FIELDNAMES) {

        Map<String, Map<String, Object>> usedFields = findUsedFieldsLuke()
        def unused = lukeFields.findAll { String fieldName, def lukeFieldInfo ->
            if (lukeFieldInfo.docs > 0) {
                return false
            } else if (fieldName ==~ overrideAsUsed) {
                return false
            } else {
                return true
            }
        }
        return unused
    }


    def collectFieldTypes() {
        def fieldTypes = xmlSchema.'**'.findAll { Node node ->
            node.name() == 'fieldType'
        }
        return fieldTypes
    }

    def collectFields() {
        def fields = xmlSchema.'**'.findAll { Node node ->
            node.name() == 'field'
        }
        return fields
    }
}
