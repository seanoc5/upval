package misc

import groovy.json.JsonSlurper
import spock.lang.Specification

class StringTemplateExperiments extends Specification {

    def "stringtemplate simple from string"() {
        given:
        String stringFromDatabase = 'Hello ${name}! -- \\$q should not be interpreted (perhaps escaped?)'
        String name = 'world'

        when:
        def engine = new groovy.text.SimpleTemplateEngine()

        then:
        'Hello world! -- $q should not be interpreted (perhaps escaped?)' == engine.createTemplate(stringFromDatabase).make([name: name]).toString()
    }

    def "stringtemplate simple from file"() {
        given:
        String app = 'myTAApp'
        String feature = 'TAFeature'
        Map variablesMap = [baseId: "${app}_${feature}",  APP:app, FEATURE_NAME:feature]
        URL simpleObjects = getClass().getResource('/components/simpleObjects.json')
        File simpleObjectsFile = new File(simpleObjects.toURI())
        String srcString = simpleObjectsFile.text

        when:
        def engine = new groovy.text.SimpleTemplateEngine()
        String output = engine.createTemplate(simpleObjectsFile).make(variablesMap).toString()
        Map map = new JsonSlurper().parseText(output)

        then:
        variablesMap.baseId == map.objects.collections[0].id

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

    def "stringtemplate index short and config from file"() {
        given:
        ConfigSlurper configSlurper = new ConfigSlurper()
        ConfigObject config = configSlurper.parse(getClass().getResource('/configs/configTypeAheadStringTemplate.groovy'))

//        File simpleObjectsFile = new File(getClass().getResource('/components/typeahead/indexpipeline.short-test.json'))
//        String srcString = simpleObjectsFile.text

        when:
        String output = new groovy.text.SimpleTemplateEngine().createTemplate(config.indexJson.text).make(config.variables).toString()
        File outFile = new File('/tmp/ta-compiled.objects.json')
        outFile.text = output

        Map map = new JsonSlurper().parseText(output)

        def objectsJson = config.objectsJson
//        def output = config.output
//        def map = config.map
        String baseId = config.variables.baseId
        def jsStage = map.stages.find{it.id == "${baseId}_IPL_JS_unwanted"}
        String jsCode = jsStage.script
        String id = map.id

        then:
        jsCode instanceof String        // not an array of Strings
        baseId+'_IPL' == id

    }


    def "stringtemplate ta-objects and config from file"() {
        given:
        JsonSlurper jsonSlurper = new JsonSlurper()
        ConfigSlurper configSlurper = new ConfigSlurper()
        ConfigObject config = configSlurper.parse(getClass().getResource('/configs/configTypeAheadStringTemplate.groovy'))

        File taObjectsFile = new File(getClass().getResource('/components/typeahead/ta-objects.json').toURI())
        String srcString = taObjectsFile.text
        Map foo = jsonSlurper.parseText(srcString)

        def engine = new groovy.text.SimpleTemplateEngine()
        String output = engine.createTemplate(srcString).make(config.variables).toString()
        Map map = jsonSlurper.parseText(output)
        List collections = map.objects.collections
        List indexPipelines = map.objects.indexPipelines
        List queryPipelines = map.objects.queryPipelines


        when:
        def objectsJson = config.objectsJson
        def output2 = config.output
        def map2 = config.map
        String baseId = config.variables.baseId
        def jsStage = map.stages.find{it.id == "${baseId}_IPL_JS_unwanted"}
        String jsCode = jsStage.script
        String id = map.id

        then:
        jsCode instanceof String        // not an array of Strings
        baseId+'_IPL' == id

    }

}
