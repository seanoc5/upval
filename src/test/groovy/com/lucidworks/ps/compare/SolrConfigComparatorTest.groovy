package com.lucidworks.ps.compare

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

class SolrConfigComparatorTest extends Specification {

    def "check schemas"() {
        given:
        XmlParser parser = new XmlParser()

        def left = getClass().getClassLoader().getResource("resources/templates/configsets/fusion-3.1.5/basic_configs/conf/managed-schema").toURI()
        def right = getClass().getClassLoader().getResource("resources/templates/configsets/fusion-4.2.6/_default/conf/managed-schema").toURI()

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
