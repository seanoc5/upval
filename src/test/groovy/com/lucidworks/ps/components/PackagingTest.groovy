package com.lucidworks.ps.components

import com.lucidworks.ps.clients.FusionClient
import spock.lang.Specification
/**
 * test suite to cover component packing operations
 */
class PackagingTest extends Specification {
    def "create simple query pipeline package with variable substitution"() {
        given:
        FusionClient fusionClient = new FusionClient('http://newmac:8764', 'sean', 'pass1234', 'test')

        when:
        def qryp = fusionClient.getQueryPipeline('test')

        def foo = fusionClient.createImportableZipArchive()

        then:
        configSetCollection.deploymentName == 'test'
        configSetCollection.configsetMap.keySet().toArray() == ['test', 'test_signals', 'test_signals_aggr']

    }

}
