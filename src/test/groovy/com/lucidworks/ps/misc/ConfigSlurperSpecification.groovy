package com.lucidworks.ps.misc

import spock.lang.Specification

/**
 * very experimental test to explore Groovy ConfigSlurper
 * @see ConfigSlurper
 */
class ConfigSlurperSpecification extends Specification {
    String configString = '''
dev {
    startLink = 'http://www.lucidworks.com:8764/api'
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
