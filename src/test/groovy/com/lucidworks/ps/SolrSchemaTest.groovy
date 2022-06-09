package com.lucidworks.ps

import com.lucidworks.ps.solr.SolrSchema
import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/6/22, Monday
 * @description:
 */

class SolrSchemaTest extends Specification {

    def "parse schema fragment"() {
        given:
        def schemaSource = getClass().getResource('/schema-min.xml')
        def lukeSource = getClass().getResource('/luke-min.json')

        when:
        SolrSchema schema = new SolrSchema(schemaSource)
        def lukeMap = schema.parseLukeOutput(lukeSource)
        def usedFields = schema.findUsedFieldsLuke()
        def unUsedFields = schema.findUnusedFields()


        then:
        schema.fieldTypes.size() == 2
        schema.definedFields.size() == 2
        schema.lukeFields.size() == 4
        unUsedFields.size() == 1
        unUsedFields.keySet()[0]=='fubar'


    }

    def "ParseSchema"() {
        given:
        def schemaSource = getClass().getResource('/f4.basic.managed-schema.xml')

        when:
        SolrSchema schema = new SolrSchema(schemaSource)

        then:
        schema.fieldTypes.size() == 64
        schema.definedFields.size() == 7

    }

    def "Parse Schema and add luke information"() {
        given:
        def schemaSource = getClass().getResource('/f4.basic.managed-schema.xml')
        def lukeSource = getClass().getResource('/f4.luke-output-basic.json')

        when:
        SolrSchema schema = new SolrSchema(schemaSource)
        def lukeMap = schema.parseLukeOutput(lukeSource)

        then:
        schema.fieldTypes.size() == 64
        schema.definedFields.size() == 7
        schema.lukeFields.size() == 7

    }

    def "GetUsedFields"() {
        given:
        def schemaSource = getClass().getResource('/f4.basic.managed-schema.xml')
        def lukeSource = getClass().getResource('/f4.luke-output-basic.json')

        when:
        SolrSchema schema = new SolrSchema(schemaSource)
        def lukeMap = schema.parseLukeOutput(lukeSource)
        def usedFields = schema.findUsedFieldsLuke()
        def unUsedFields = schema.findUnusedFields()

        then:
        schema.fieldTypes.size() == 64
        schema.definedFields.size() == 7
        schema.lukeFields.size() == 7
        usedFields.keySet().size() == 5
        usedFields.keySet().toList() == [ '_text_', '_version_', 'body', 'id', 'title']
        unUsedFields.keySet().toList() == [ 'body_str', 'title_str']

    }
}
