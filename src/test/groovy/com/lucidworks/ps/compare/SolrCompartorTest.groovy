package com.lucidworks.ps.compare

import groovy.xml.XmlParser
import spock.lang.Specification
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/28/22, Saturday
 * @description:
 */

/**
 * Test custom upval SolrConfigComparator class
 */
class SolrCompartorTest extends Specification {

    def "check schemas"() {
        given:
        XmlParser parser = new XmlParser()

//        def attribsForPath = ~/(name|id|source|dest|type|class)/
        def attribsForPath = [
                copyField: ~/source|dest/,
                analyzer : ~/type/,
                tokenizer: ~/class/,
                filter   : ~/class/,
                ''       : ~/(name|id)/
        ]

        def leftResource = getClass().getResourceAsStream('/f3.sample_tech.managed-schema.xml')
        def rightResource = getClass().getResourceAsStream('/f4.sample_tech.managed-schema.xml')
        Node leftSchema = parser.parse(leftResource)
        Node rightSchema = parser.parse(rightResource)

        when:
        def results = SolrConfigComparator.compareXmlObjects(leftSchema, rightSchema, attribsForPath)

        then:
        results.sharedIds.size() == 387
        results.leftOnlyIds.size() == 40
        results.rightOnlyIds.size() == 34
        results.leftOnlyIds[0] == '/schema[name:example]/dynamicField[name:*_pi]'
        results.rightOnlyIds[0] == '/schema[name:example]/dynamicField[name:*_s_ns]'
    }


    def "check solr configs basic"() {
        given:
        XmlParser parser = new XmlParser()
        def attribsForPath = [
                lib             : ~/dir|regex/,
                ''              : ~/(name|id)/
        ]

        def leftResource = getClass().getResourceAsStream('/f3.sample_tech.solrconfig.xml')
        def rightResource = getClass().getResourceAsStream('/f4.sample_tech.solrconfig.xml')
        Node left = parser.parse(leftResource)
        Node right = parser.parse(rightResource)

        when:
        def results = SolrConfigComparator.compareXmlObjects(left, right, attribsForPath)

        then:
        results.sharedIds.size() == 238
        results.leftOnlyIds.size() == 38
        results.rightOnlyIds.size() == 1
        results.leftOnlyIds[0] == '/config/lib[dir:${solr.install.dir:../../../..}/contrib/clustering/lib/, regex:.*\\.jar]'
        results.rightOnlyIds[0] == '/config/circuitBreaker'

    }


    def "check solr configs advanced naming with custom attribute capture by node name"() {
        given:
        XmlParser parser = new XmlParser()
        def attribsForPath = [
                lib             : ~/dir|regex/,
                directoryFactory: ~/class/,
                codecFactory    : ~/class/,
                updateHandler   : ~/class/,
                filterCache     : ~/class/,
                queryResultCache     : ~/class/,
                documentCache     : ~/class/,
                filter          : ~/class/,
                ''              : ~/(name|id)/
        ]

        def leftResource = getClass().getResourceAsStream('/f3.sample_tech.solrconfig.xml')
        def rightResource = getClass().getResourceAsStream('/f4.sample_tech.solrconfig.xml')
        Node left = parser.parse(leftResource)
        Node right = parser.parse(rightResource)

        when:
        def results = SolrConfigComparator.compareXmlObjects(left, right, attribsForPath)

        then:
        results.sharedIds.size() == 238
        results.leftOnlyIds.size() == 41
        results.rightOnlyIds.size() == 4
        results.leftOnlyIds[0] == '/config/lib[dir:${solr.install.dir:../../../..}/contrib/clustering/lib/, regex:.*\\.jar]'
        results.rightOnlyIds[0] == '/config/query/filterCache'

    }

}
