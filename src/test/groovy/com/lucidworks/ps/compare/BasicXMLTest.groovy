package com.lucidworks.ps.compare

import com.lucidworks.ps.upval.Helper
import groovy.xml.XmlParser
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import spock.lang.Specification

import static com.lucidworks.ps.upval.Helper.diveXmlPath

class BasicXMLTest extends Specification {
    def xmlString = '''
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
    def xml

    void setup() {

    }

    def path(GPathResult node) {
        def result = [node.name()]
        def pathWalker = [hasNext: { -> node.parent() != node }, next: { -> node = node.parent() }] as Iterator
        (result + pathWalker.collect { it.name() }).reverse().join('/')
    }



    def "divepath all"() {
        given:
        GPathResult xml = new XmlSlurper().parseText(xmlString)
        when:
        def divePaths = diveXmlPath(xml, 0, '/')
        divePaths.each {
            println it
        }

        then:
        divePaths.size() == 15
    }


    def "Flatten all"() {
        given:
        def xml = new XmlSlurper().parseText(xmlString)
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


    def "Flatten leaf nodes"() {
        given:
        def xml = new XmlSlurper().parseText(xmlString)

        when:
        def leaves = xml.'**'.findAll { it.children().size() == 0 }

        then:
        leaves.size() > 5
    }


    def "Flatten demo schema files"() {
        given:
        XmlParser parser = new XmlParser()
        def leftResource = getClass().getResourceAsStream('/f3.sample_tech.managed-schema.xml')
        def rightResource = getClass().getResourceAsStream('/f4.sample_tech.managed-schema.xml')
        def leftSchema = parser.parse(leftResource)
        def rightSchema = parser.parse(rightResource)

        when:
        def flatLeft = Helper.diveXmlPath(leftSchema)
        def flatRight = Helper.diveXmlPath(rightSchema)

        then:
        flatLeft.size() == 9
        flatRight.size() == 11
        flatLeft == flatRight
    }

}
