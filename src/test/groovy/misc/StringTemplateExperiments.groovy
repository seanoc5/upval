package misc

import groovy.json.JsonSlurper
import spock.lang.Specification

class StringTemplateExperiments extends Specification {

    def "stringtemplate simple from string"() {
        given:
        String stringFromDatabase = 'Hello ${name}! -- $q should not be interpreted (perhaps escaped?) '
        String name = 'world'

        when:
        def engine = new groovy.text.SimpleTemplateEngine()

        then:
        'Hello world!' == engine.createTemplate(stringFromDatabase).make([name: name]).toString()
    }

    def "stringtemplate simple from file"() {
        given:
        Map variablesMap = [baseId: 'MyApp_MyTaName', 'foo.bar': "myFooBar"]
        URL simpleObjects = getClass().getResource('/components/simpleObjects.json')
        File simpleObjectsFile = new File(simpleObjects.toURI())
        String srcString = simpleObjectsFile.text

        when:
        def engine = new groovy.text.SimpleTemplateEngine()
        String output = engine.createTemplate(simpleObjectsFile).make(variablesMap).toString()
        Map map = new JsonSlurper().parseText(output)

        then:
        baseId == map.objects.collections[0].id

    }

    def "stringtemplate ta-objects from file"() {
        given:
        Map variables = [
                APP         : "MyApp",
                COLLECTION  : "MyAppColl",
                FEATURE_NAME: "TA_Feature",
                baseId      : 'MyBaseId',
//                ZKHOST           : "myzk-0.myzk-headless:2181,myzk-1.myzk-headless:2181,myzk-2.myzk-headless:2181",
//                ZKHOST           : "myzk",
//                SIGNALS_AGGR_COLL: "MyAppColl_signals_aggr",
//                TYPE_FIELD_1     : "brand, flattenedbrandPath_s, brandUrl_s, brandImageUrl_s ",
//                TYPE_FIELD_2     : "TYPE_FIELD_2",
//                TYPE_FIELD_3     : "TYPE_FIELD_3",
//                TYPE_FIELD_4     : "TYPE_FIELD_4",
//                TYPE_FIELD_5     : "TYPE_FIELD_5",
        ]
        File srcFile = new File(getClass().getResource('/components/simpleObjects.json').toURI())
        String objectsJson = srcFile.text


        when:
        def engine = new groovy.text.SimpleTemplateEngine()
        String output = engine.createTemplate(objectsJson).make(variables).toString()
        Map map = new JsonSlurper().parseText(output)
        List collections = map.objects.collections
        List indexPipelines = map.objects.indexPipelines
        List queryPipelines = map.objects.queryPipelines

        then:
        collections[0].id == 'MyApp_TA_Feature'

    }


}
