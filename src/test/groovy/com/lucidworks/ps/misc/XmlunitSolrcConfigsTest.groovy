package com.lucidworks.ps.misc


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

/**
 * @deprecated -- testing xmlunit
 * did not like the functionality, so rolled a custom SolrConfigComparator
 */
class XmlunitSolrcConfigsTest extends Specification {

    /**
     * this test is left as an example of an approach I tried, and found too verbose, and not focused enough--feel free to ignore this
     */
    def "check schemas via DiffBuilder -- discarded approach"() {
        given:
        XmlParser parser = new XmlParser()
        def leftResource = getClass().getResourceAsStream('/f3.sample_tech.solrconfig.xml')
        def rightResource = getClass().getResourceAsStream('/f4.sample_tech.solrconfig.xml')

        when:
//        def left = Input.fromPath(leftResource).build();
        def left = Input.fromStream(leftResource).build();
        def right = Input.fromStream(rightResource).build();
        DifferenceEngine diff = new DOMDifferenceEngine();

        Diff myDiff = DiffBuilder.compare(left)
                .withTest(right)
                .ignoreWhitespace()
                .ignoreComments()
                .normalizeWhitespace()
                .build()

        Iterator<org.xmlunit.diff.Difference> iter = myDiff.getDifferences().iterator();
        List diffs = []
        iter.each { org.xmlunit.diff.Difference d ->
            println "Difference: $d (control: ${d.comparison.controlDetails}) -- (${d.comparison.testDetails})"
            diffs << d
        }

        def similarities = myDiff.getDifferences()


        then:
        diffs.size() == 450
    }
}
