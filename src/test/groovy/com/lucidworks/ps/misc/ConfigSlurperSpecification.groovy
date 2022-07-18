package com.lucidworks.ps.misc

import spock.lang.Specification

/**
 * very experimental test to explore Groovy ConfigSlurper
 * @deprecated possibly revisit later...
 * @see ConfigSlurper
 */
class ConfigSlurperSpecification extends Specification {
    String configString = '''
dev {
    startLink = 'http://www.lucidworks.com:8764/api'
    foo = 'bar'
}

def getProtocol(){
    return 'https'
}
'''


    def "simple test config"() {
        given:
        ConfigSlurper configSlurper = new ConfigSlurper()

        when:
        def config = configSlurper.parse(configString)

        then:
        config.dev.foo == 'bar'
    }

    /**
     * this is not working, not sure if it CAN work, but this test is a simple starting point for allowing config-defined functionality
     * 2022-06-21 SoC
     */
    def "method defined in config should be callable"() {
        given:
        ConfigSlurper configSlurper = new ConfigSlurper()

        when:
        def config = configSlurper.parse(configString)

        then:
        config.getProtocol == 'https'
    }

    def "should load rules from config file with configslurper"() {
        given:
        URL configUrl = getClass().getResource('/configurations/deployDCLargeConfiguration.groovy')
        File config = new File(configUrl.toURI())
        ConfigSlurper configSlurper = new ConfigSlurper()


        when:
        ConfigObject cfgObject = configSlurper.parse(configUrl)
        Map rules = cfgObject.rules
        Map copyRule0 = rules.copy[0]

        then:
        rules instanceof Map
        rules.copy instanceof List
        copyRule0 instanceof Map
        copyRule0.keySet().toList() == ['sourcePath', 'sourceItemPattern', 'destinationExpression', ]

    }
}
