package com.lucidworks.ps.promotion


import groovy.json.JsonSlurper
import spock.lang.Specification
/**
 * Less of a test suite, more of seeing how things can work...
 */
class BasicPromotionTests extends Specification {

    def "compare dev-stage ldap-acls promotion"() {
        given:
        JsonSlurper slurper = new JsonSlurper()

        when:
        Map dev = slurper.parse(getClass().getResourceAsStream('/promotions/F5.ldap-acls.dev.json'))
        Map stage = slurper.parse(getClass().getResourceAsStream('/promotions/F5.ldap-acls.stage.json'))

        then:
        dev.properties.searchProperties.apiQueryRowLimit == 15000
        stage.properties.searchProperties.apiQueryRowLimit == 1000
        dev.properties.collection == 'Search-dev'
        stage.properties.collection == 'Search-stage'
        dev.coreProperties.someCoreProperty == 'dev-value'
        stage.coreProperties.someCoreProperty == 'stage-value'
    }


}
