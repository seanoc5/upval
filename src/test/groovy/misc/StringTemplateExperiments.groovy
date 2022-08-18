package misc

import groovy.json.JsonSlurper
import spock.lang.Specification

class StringTemplateExperiments extends Specification {

    def "stringtemplate simple from string"() {
        given:
        String stringFromDatabase = 'Hello ${name}!'
        String name = 'world'

        when:
        def engine = new groovy.text.SimpleTemplateEngine()

        then:
        'Hello world!'== engine.createTemplate(stringFromDatabase).make([name:name]).toString()
    }

    def "stringtemplate simple from file"() {
        given:
        URL simpleObjects = getClass().getResource('/components/simpleObjects.json')
        File simpleObjectsFile = new File(simpleObjects.toURI())
        String srcString = simpleObjectsFile.text

        String baseId = 'MyApp_MyTaName'

        when:
        def engine = new groovy.text.SimpleTemplateEngine()
        String output = engine.createTemplate(simpleObjectsFile).make([baseId:baseId]).toString()
        Map map = new JsonSlurper().parseText(output)

        then:
        baseId == map.objects.collections[0].id

    }

    def "stringtemplate ta-objects from file"() {
        given:
        URL simpleObjects = getClass().getResource('/components/ta-objects.json')
        File simpleObjectsFile = new File(simpleObjects.toURI())
        String srcString = simpleObjectsFile.text

        String baseId = 'MyApp_MyTaName'

        when:
        def engine = new groovy.text.SimpleTemplateEngine()
        String output = engine.createTemplate(simpleObjectsFile).make([baseId:baseId]).toString()
        Map map = new JsonSlurper().parseText(output)

        then:
        baseId == map.objects.collections[0].id

    }


}
