package com.lucidworks.ps.config


import groovy.cli.picocli.OptionAccessor
import spock.lang.Specification

/**
 * very experimental test to explore Groovy ConfigSlurper
 * @see ConfigSlurper
 */
class ConfigSlurperTest extends Specification {
    String configString = '''
fusionClient {
    username = 'admin'
    password = 'password123'
    fusionUrl = 'https://test.lucidworks.com:6764'
}

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
    myMap {
        a = 'one'
        b='two'
    }
    
   queryParams = [
        [
           key = "qf",
           value = "query_t",
        ],
        [
           key = "pf",
           value = "query_t^50",
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

    def "simple test config with setBinding"() {
        given:
        ConfigSlurper configSlurper = new ConfigSlurper()
        String username = 'admin'
//        println('What is your name?')
//        def username = System.in.newReader().readLine()
//        def username = System.console().readLine 'What is your name?'
        configSlurper.setBinding([mykey:'myvalue', username:username])

        when:
        def config = configSlurper.parse(configString)

        then:
        config.fusionClient.username == username
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
        URL fusionUrl = new URL(config.fusionClient.fusionUrl)

        then:
        fusionUrl.getProtocol() == 'https'
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
        URL cfgSource = getClass().getResource('/configs/configTypeAhead.groovy')
        println("Config source: $cfgSource")
        ConfigObject config = configSlurper.parse(cfgSource)
        def ta = config.collections.typeahead

        then:
        config.appName == 'test'
        config.taName == 'mytypeahead'
        config.blobs.size() == 3
        config.collections.typeahead.size() == 6
        ta.size() == 6
        ta.id == "${config.appName}_${config.taName}"
    }

    def "should perform dynamic evalation in configslurper parse"(){
        given:
        URL baseUrl = getClass().getResource('.')
        URL configUrl = getClass().getResource('/configs/simpleConfig.groovy')
        URL simpleObjects = getClass().getResource('/components/simpleObjects.json')
        File so = new File(simpleObjects.toURI())
        File simpleCfg = new File(configUrl.toURI())
//        println "Simple objects: ${so.absolutePath} -- ${so.exists()}"
        ConfigSlurper configSlurper = new ConfigSlurper()

        String foo = 'C:\\Users\\bentc\\IdeaProjects\\upval\\src\\test\\resources\\configs\\simpleConfig.groovy'
        File fooFile = new File(foo)
//        def cfgTest = configSlurper.parse(fooFile.toURL())

        when:
        def bar = 'test'
        def cfg = configSlurper.parse(configUrl)

        then:
        cfg.test != null
        cfg.test.foo =="Testing eval: defaultAppName_myTypeahead"
        cfg.test.mydate instanceof Date


    }



    def "merge ConfigObject with CliBuilder options"(){
        given:
        ConfigObject config = new ConfigSlurper().parse(configString)

        String[] args = ['-c/tmp/imaginaryConfigFile.groovy', '-umyUser', '-pmyPassword']


        when:
        OptionAccessor options = DeployArgParser.parse(this.class.name, args, config)

        then:
        options.u

    }


}
