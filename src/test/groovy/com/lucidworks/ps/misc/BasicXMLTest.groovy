package com.lucidworks.ps.misc

import com.lucidworks.ps.transform.XmlObject
import com.lucidworks.ps.Helper
//import com.lucidworks.ps.upval.Helper
import groovy.xml.XmlParser
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import spock.lang.Specification

/**
 * exploring xml parsing & processing
 *
 */
class BasicXMLTest extends Specification {
    public static String XML_TEST_STRING = '''
<response version-api="2.0">
    <value>
        <books>
            <book available="20" id="1">
                <title>Don Quixote</title>
                <author id="1">Miguel de Cervantes</author>
            </book>
            <book available="14" id="2">
                <title>Catcher in the Rye</title>
               <author id="2">JD Salinger</author>
           </book>
           <book available="13" id="3">
               <title>Alice in Wonderland</title>
               <author id="3">Lewis Carroll</author>
           </book>
           <book available="5" id="4">
               <title>Don Quixote</title>
               <author id="4">Miguel de Cervantes</author>
           </book>
       </books>
   </value>
</response>
'''

    /**
     * hackish function to show one way to walk the tree, left here for reference, but didn't like it enough to use for real
     *@deprecated
     * @see Helper for other options
     * @param node
     * @return
     */
    def path(GPathResult node) {
        def result = [node.name()]
        def pathWalker = [hasNext: { -> node.parent() != node }, next: { -> node = node.parent() }] as Iterator
        (result + pathWalker.collect { it.name() }).reverse().join('/')
    }


    /**
     * note: difference between xmlsluper and xmlparser:
     * https://stackoverflow.com/questions/7558019/groovy-xmlslurper-vs-xmlparser
     */
    def "divepath all"() {
        given:
//        NodeChild xml = new XmlSlurper().parseText(xmlString)
        Node xml = new XmlParser().parseText(XML_TEST_STRING)
        when:
        def divePaths = XmlObject.flattenXmlPath(xml, 0, '/')
        divePaths.each {
            println it
        }

        then:
        divePaths.size() == 15
        divePaths[0] == '/response'
        divePaths[1] == '/response/value'
        divePaths[2] == '/response/value/books'
        divePaths[3] == '/response/value/books/book'
    }


    /**
     * demo a (poor?) way to walk nodes
     * @deprecated
     */
    def "should Flatten all nodes with a hackish approach"() {
        given:
        def xml = new XmlSlurper().parseText(XML_TEST_STRING)
        when:
        def flatNodes = xml.'**'.findAll { it }
        def flatPaths = flatNodes.collect { path(it) }


        then:
        flatPaths[0] == 'response'
        flatPaths[1] == 'value'
        flatPaths[2] == 'books'
        flatPaths[3] == 'books/book'
        flatPaths[5] == 'books/book/author'
        flatPaths.size() == 15
    }


    def "should Flatten leaf nodes of xml string to expected node count"() {
        given:
        def xml = new XmlSlurper().parseText(XML_TEST_STRING)

        when:
        def leaves = xml.'**'.findAll { it.children().size() == 0 }

        then:
        leaves.size() > 5
    }


    def "should Flatten demo schema files with expected left-right count differences"() {
        given:
        XmlParser parser = new XmlParser()
        def leftResource = getClass().getResourceAsStream('/f3.sample_tech.managed-schema.xml')
        def rightResource = getClass().getResourceAsStream('/f4.sample_tech.managed-schema.xml')
        def leftSchema = parser.parse(leftResource)
        def rightSchema = parser.parse(rightResource)

        when:
        def flatLeft = XmlObject.flattenXmlPath(leftSchema)
        def flatRight = XmlObject.flattenXmlPath(rightSchema)

        then:
        flatLeft.size() == 444
        flatRight.size() == 438

    }

}
