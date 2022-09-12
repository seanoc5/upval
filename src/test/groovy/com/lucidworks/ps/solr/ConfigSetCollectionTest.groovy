package com.lucidworks.ps.solr

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.model.fusion.Application
import com.lucidworks.ps.model.solr.ConfigSet
import com.lucidworks.ps.model.solr.ConfigSetCollection
import spock.lang.Specification

class ConfigSetCollectionTest extends Specification {
    def "constructor with app export-pruned"() {
        given:
        File appZip = new File(getClass().getResource('/apps/test.f5.partial.zip').toURI())
        Application app = new Application(appZip)

        when:
        ConfigSetCollection configSetCollection = app.configsets
        ConfigSet testConfigSet = configSetCollection.configsetMap['test']

        then:
        configSetCollection.deploymentName == 'test'
        configSetCollection.configsetMap.keySet().toArray() == ['test', 'test_signals', 'test_signals_aggr']

    }

    def "get configset from running fusion"() {
        given:
        FusionClient fusionClient = new FusionClient()

        when:
        ConfigSetCollection configSetCollection = app.configsets
        ConfigSet testConfigSet = configSetCollection.configsetMap['test']

        then:
        configSetCollection.deploymentName == 'test'
        configSetCollection.configsetMap.keySet().toArray() == ['test', 'test_signals', 'test_signals_aggr']

    }

}
