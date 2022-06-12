package com.lucidworks.ps.compare


import spock.lang.Specification
/**
 * Less of a test suite, more of seeing how things can work...
 */
class CompareObjectsViaConfigSlurperTest extends Specification {


    def "Simple object compare"() {
        given:
        def configSource = getClass().getResource('/testObjects.config.groovy')
        ConfigSlurper slurper = new ConfigSlurper('simple')     // get definitions from 'simple' environment
        def config = slurper.parse(configSource)
        println "Config environment: ${slurper.getEnvironment()}"

        when:
        def left = config.leftobject
        def right = config.rightobject
        BaseComparator comparator = new BaseComparator(left, right)
        CompareObjectsResults results = comparator.compare('SimpleConfigObject')

        then:
        results.leftOnlyKeys.size() == 0
        results.rightOnlyKeys.size() == 1
        results.rightOnlyKeys[0] == 'parentB.Child3B'
    }

    def "Moderate object compare"() {
        given:
        def configSource = getClass().getResource('/testObjects.config.groovy')
        ConfigSlurper slurper = new ConfigSlurper('moderate')     // get definitions from 'simple' environment
        def config = slurper.parse(configSource)
        println "Config environment: ${slurper.getEnvironment()}"

        when:
        def left = config.leftobject
        def right = config.rightobject
        BaseComparator comparator = new BaseComparator(left, right)
        CompareObjectsResults results = comparator.compare('ModerateConfigObject')

        then:
        results.leftOnlyKeys.size() == 6
        results.leftOnlyKeys[0] == 'parentC.childTree1.childNode1'
        results.leftOnlyKeys[1] == 'parentC.childTree2.childNode2.grandchildNode1'
        results.leftOnlyKeys[5] == 'parentListD.[3]'

        results.rightOnlyKeys.size() == 5
        results.rightOnlyKeys[0] == 'parentB.Child3B'
        results.rightOnlyKeys[1] == 'parentListE.[0]'
        results.rightOnlyKeys[4] == 'parentListE.[2].bar'
    }

    def "Advanced object compare"() {
        given:
        def configSource = getClass().getResource('/testObjects.config.groovy')
        ConfigSlurper slurper = new ConfigSlurper('moderate')     // get definitions from 'simple' environment
        def config = slurper.parse(configSource)
        println "Config environment: ${slurper.getEnvironment()}"

        when:
        def left = config.leftobject
        def right = config.rightobject
        BaseComparator comparator = new BaseComparator(left, right)
        CompareObjectsResults results = comparator.compare('ModerateConfigObject')

        then:
        results.leftOnlyKeys.size() == 6
        results.leftOnlyKeys[0] == 'parentC.childTree1.childNode1'
        results.leftOnlyKeys[1] == 'parentC.childTree2.childNode2.grandchildNode1'
        results.leftOnlyKeys[5] == 'parentListD.[3]'

        results.rightOnlyKeys.size() == 5
        results.rightOnlyKeys[0] == 'parentB.Child3B'
        results.rightOnlyKeys[1] == 'parentListE.[0]'
        results.rightOnlyKeys[4] == 'parentListE.[2].bar'
    }


}
