package com.lucidworks.ps.solr

import com.lucidworks.ps.fusion.Application
import spock.lang.Specification

class ConfigSetCollectionTest extends Specification {
    def "constructor with app export-pruned"() {
        given:
        File appZip = new File(getClass().getResource('/apps/test.f5.partial.zip').toURI())
        Application app = new Application(appZip)

        when:
        ConfigSetCollection configsets = app.configsets

        then:
        configsets.deploymentName == 'test'
        configsets.configsetMap.keySet().toArray() == ['test', 'test_signals', 'test_signals_aggr']

    }
}
