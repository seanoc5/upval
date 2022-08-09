package com.lucidworks.ps.misc

import spock.lang.Specification

/**
 * very experimental test to explore Groovy ConfigSlurper
 * @deprecated possibly revisit later...
 * @see ConfigSlurper
 */
class ConfigSlurperTest extends Specification {
    String configString = '''
dev {
    startLink = 'http://www.lucidworks.com:8764/api'
    foo = 'bar'
    testKey = "$mykey"
    usernam = "$name"
    myArrayNumbers = [1,2,3]
    myArrayMaps = [
        [a:1, b:2],
        [c:3, d:4],
    ]
    stages = [
        [
          id: "foo1",
          ref: "lib/index/FusionServiceLib.js",
        ],
        [
          type: "managed-js-index",
          skip: false,
        ]
    ]
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
        config.dev.startLink == 'http://www.lucidworks.com:8764/api'
        config.dev.testKey == '[:]'         // empty/unbound
        config.dev.myKey.size() == 0         // empty/unbound
        config.dev.myArrayNumbers == [1,2,3]         // empty/unbound
        config.dev.myArrayMaps[0].keySet().toList() == ['a','b']         // empty/unbound

    }

    def "simple test config with override"() {
        given:
        ConfigSlurper configSlurper = new ConfigSlurper()
//        println('What is your name?')
//        def username = System.in.newReader().readLine()
//        def username = System.console().readLine 'What is your name?'
        configSlurper.setBinding([mykey:'myvalue', name:username])

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
        URL configUrl = getClass().getResource('/configs/configDeployDCLarge.groovy')
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
    def "should load typeahead rules from config file with configslurper"() {
        given:
        Map myBindings = [appName:'test', taName:'mytypeahead']
        ConfigSlurper configSlurper = new ConfigSlurper()
        configSlurper.setBinding(myBindings)


        when:
        ConfigObject config = configSlurper.parse(getClass().getResource('/configs/configTypeAhead.groovy'))
        def ta = config.collections.typeahead

        then:
        config.appName == 'test'
        config.taName == 'mytypeahead'
        config.blobs.size() == 3
        config.collections.typeahead.size() == 6
        ta.size() == 6
        ta.id == "${config.appName}_${config.taName}"

    }


}
