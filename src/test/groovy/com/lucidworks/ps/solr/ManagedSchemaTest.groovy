package com.lucidworks.ps.solr

import com.lucidworks.ps.model.solr.ManagedSchema
import spock.lang.Specification
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/6/22, Monday
 * @description:
 */

class ManagedSchemaTest extends Specification {

    def "parse schema fragment with luke in constructor"() {
        given:
        def schemaSource = getClass().getResource('/schema-min.xml')
        def lukeSource = getClass().getResource('/luke-min.json')

        when:
        ManagedSchema schema = new ManagedSchema(schemaSource, schemaSource.toString(), lukeSource)
        def usedFields = schema.findUsedFieldsLuke()
        def unUsedFields = schema.findUnusedLukeFields()


        then:
        schema.fieldTypes.size() == 2
        schema.schemaFields.size() == 2
        schema.knownfields.size() == 5
        schema.lukeFields.size() == 4
        usedFields.keySet().toList() == ['title', '_version_', 'body']
        unUsedFields.size() == 1
        unUsedFields.keySet()[0]=='exampleNoDocs'
        unUsedFields['exampleNoDocs'].type == 'text_general'
        unUsedFields['exampleNoDocs'].docs == 0
        unUsedFields['exampleNoDocs'].schema == 'ITS-UM------------'


    }
    def "parse schema fragment adding luke after constructor"() {
        given:
        def schemaSource = getClass().getResource('/schema-min.xml')
        def lukeSource = getClass().getResource('/luke-min.json')

        when:
        ManagedSchema schema = new ManagedSchema(schemaSource, schemaSource.file)
        def lukeMap = schema.parseLukeOutput(lukeSource)
        def usedFields = schema.findUsedFieldsLuke()
        def unUsedFields = schema.findUnusedLukeFields()


        then:
        schema.fieldTypes.size() == 2
        schema.schemaFields.size() == 2
        schema.lukeFields.size() == 4
        usedFields.keySet().toList() == ['title', '_version_', 'body']
        unUsedFields.size() == 1
        unUsedFields.keySet()[0]=='exampleNoDocs'
    }

    def "ParseSchema"() {
        given:
        def schemaSource = getClass().getResource('/f4.basic.managed-schema.xml')

        when:
        ManagedSchema schema = new ManagedSchema(schemaSource, schemaSource.file)

        then:
        schema.fieldTypes.size() == 64
        schema.schemaFields.size() == 7

    }

    def "Parse Schema and add luke information"() {
        given:
        def schemaSource = getClass().getResource('/f4.basic.managed-schema.xml')
        def lukeSource = getClass().getResource('/f4.luke-output-basic.json')

        when:
        ManagedSchema schema = new ManagedSchema(schemaSource, schemaSource.toString(), lukeSource)
//        def lukeMap = schema.parseLukeOutput(lukeSource)

        then:
        schema.fieldTypes.size() == 64
        schema.schemaFields.size() == 7
        schema.lukeFields.size() == 9

    }

    def "GetUsedFields"() {
        given:
        def schemaSource = getClass().getResource('/f4.basic.managed-schema.xml')
        def lukeSource = getClass().getResource('/f4.luke-output-basic.json')

        when:
        ManagedSchema schema = new ManagedSchema(schemaSource, schemaSource.file)
        def lukeMap = schema.parseLukeOutput(lukeSource)
        def usedFields = schema.findUsedFieldsLuke()
        def unUsedFields = schema.findUnusedLukeFields()

        then:
        schema.fieldTypes.size() == 64
        schema.schemaFields.size() == 7
        schema.lukeFields.size() == 9
        usedFields.keySet().size() == 7
        usedFields.keySet().toList() == [ '_text_', '_version_', 'body', 'id', 'title', 'tag_ss', 'title_t']
        unUsedFields.keySet().toList() == [ 'body_str', 'title_str']

    }

    def "check dynamic fields"() {
        given:
        def schemaSource = getClass().getResource('/f4.basic.managed-schema.xml')
        def lukeSource = getClass().getResource('/f4.luke-output-basic.json')

        when:
        ManagedSchema schema = new ManagedSchema(schemaSource, schemaSource.toString(), lukeSource)
        def dynamicFields = schema.schemaDynamicFieldDefinitions
        def unusedDynamic = schema.findUnusedDynamicfields()

        then:
        dynamicFields.size() == 68
        unusedDynamic.size() == 65

    }

    def "check other things"() {
        given:
        def schemaSource = getClass().getResource('/f4.basic.managed-schema.xml')
        def lukeSource = getClass().getResource('/f4.luke-output-basic.json')

        when:
        ManagedSchema schema = new ManagedSchema(schemaSource, schemaSource.toString(), lukeSource)
        def dynamicFields = schema.schemaDynamicFieldDefinitions
        def unusedDynamic = schema.findUnusedDynamicfields()


        then:
        dynamicFields.size() == 68
        unusedDynamic.size() == 65

    }

}
