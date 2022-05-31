package com.lucidworks.ps.upval

import spock.lang.Specification

class ConfigSpecification extends Specification {
    String configString = '''
dev {
    startLink = 'http://www.lucidworks.com:8764/api'
}

def getProtocol(){
    return 'https'
}
'''
//    void setup() {
//    }

    def "simple test config"() {
        given:
        ConfigSlurper configSlurper = new ConfigSlurper()

        when:
        def config = configSlurper.parse(configString)

        then:
        config.dev.foo == 2
    }

    def "test method config"() {
        given:
        ConfigSlurper configSlurper = new ConfigSlurper()

        when:
        def config = configSlurper.parse(configString)

        then:
        config.getProtocol() == 'https'
    }

}
