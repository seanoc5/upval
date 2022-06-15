package com.lucidworks.ps.solr


import spock.lang.Specification

class ConfigSetCollectionTest extends Specification {
    def "constructor with app export-pruned"() {
        given:
        File appZip = new File(getClass().getResource('/apps/test.f5.partial.zip').toURI())
//        Application app = new Application(appZip)

        when:
//        def configSetMap = app.getConfigsets()
        ConfigSetCollection configsets = new ConfigSetCollection(appZip)

        then:
//        configSetList.size() == 144
        'foo' == 'bar'

    }
}
