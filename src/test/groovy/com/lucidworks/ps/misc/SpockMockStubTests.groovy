package com.lucidworks.ps.misc


import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * Less of a test suite, more of seeing how things can work...
 * exploring <a href='https://spockframework.org/spock/docs/1.0/interaction_based_testing.html'>mock</a> (and perhaps stubs?)
 */
class SpockMockStubTests extends Specification {

    def "check out simplest mock component"() {
        when:
        // ignore the implementation details here--this is merely a placeholder for some operation yet to be developed
        def component = checkoutComponent('/components/parser.system.json')

        then:
        // placeholder tests: replace these with more pertinent checks that show what a successful check entails...
        component.id == '_system'
        component.parserStages.size() == 8

        component.parserStages[0].type == 'archive'
        component.parserStages[0].enabled == true

        isCompoundComponent(component) == false
    }

    def "check out compound mock component"() {
        when:
        // ignore the implementation details here--this is merely a placeholder for some operation yet to be developed
        Map<String, Map<String, Object>> component = checkoutComponent('/components/datasource.compound.demo.json')

        then:
        // placeholder tests: replace these with more pertinent checks that show what a successful check entails...
        component.keySet().toArray() == ['dataSource', 'indexPipeline', 'parser']
        component.dataSource.size() == 7
        component.dataSource.id == 'demo_solr'

        component.indexPipeline.keySet().size() == 3
        component.indexPipeline.keySet().toArray() == ['id', 'stages', 'properties']
        component.indexPipeline.stages.size() == 1
        component.indexPipeline.properties.size() == 1

        !component.parser

        isCompoundComponent(component) == true
    }


    /**
     * Mock psuedo-functionality as placeholder for a real checkout process (way) in the future
     * @param comonentName
     * @return
     */
    def checkoutComponent(String comonentName) {
        JsonSlurper slurper = new JsonSlurper()
        // ignore this functionality, merely a placeholder for future code/implementation
        def component = slurper.parse(getClass().getResourceAsStream(comonentName))
    }


    // placeholder MOCK check to demo types of things to consider when checking out a component
    boolean isCompoundComponent(def component) {
        boolean compound = false
        if (component instanceof Map) {
            if (component.id == '_system') {
                compound = false
            } else if(component.dataSource){
                // mock code/condition above -- not real implementation
                compound = true
            } else {
                //other code/checks could go here, but this is a mock test, no need for completeness, just mocking TYPEs of actions/operations we MIGHT employ
            }
        }
        return compound
    }
}
