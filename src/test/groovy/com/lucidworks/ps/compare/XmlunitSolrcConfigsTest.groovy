package com.lucidworks.ps.compare

import com.lucidworks.ps.upval.Helper
import groovy.xml.XmlParser
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import org.xmlunit.diff.DOMDifferenceEngine
import org.xmlunit.diff.Diff
import org.xmlunit.diff.DifferenceEngine
import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/28/22, Saturday
 * @description:
 */

class XmlunitSolrcConfigsTest extends Specification {

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


    def "check solr configs"() {
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
        results.sharedIds.size() == 387
        results.leftOnlyIds.size() == 40
        results.rightOnlyIds.size() == 34
        results.leftOnlyIds[0] == '/schema[name:example]/dynamicField[name:*_pi]'
        results.rightOnlyIds[0] == '/schema[name:example]/dynamicField[name:*_s_ns]'

    }

    def "check schemas via DiffBuilder"() {
        given:
        XmlParser parser = new XmlParser()

        // todo -- fix loading resources
        this.getClass().getResource('.')
        def foo = Helper.class.getResource("resources/templates/configsets/fusion-3.1.5/basic_configs/conf/managed-schema")
        def left = foo.toURI()
        def right = Helper.class.getResource("resources/templates/configsets/fusion-4.2.6/_default/conf/managed-schema").toURI()

        when:
        def control = Input.fromPath(tsPath).build();
        def test = Input.fromFile(dsFile).build();
        DifferenceEngine diff = new DOMDifferenceEngine();

        Diff myDiff = DiffBuilder.compare(control)
                .withTest(test)
                .ignoreWhitespace()
                .ignoreComments()
                .normalizeWhitespace()
                .build()

//        Iterator<org.xmlunit.diff.Difference> iter = myDiff.getDifferences().iterator();
//        iter.each { org.xmlunit.diff.Difference d ->
//            log.info "Difference: $d (control: ${d.comparison.controlDetails}) -- (${d.comparison.testDetails})"
//        }
//
//        log.info "done..."

        then:
        1 == 1
    }
}
