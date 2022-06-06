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


    def "divepath all"() {
        given:
        def xml = new XmlSlurper().parseText(xmlString)
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
        flatPaths.sort().each {
            println it
        }

        then:
        flatNodes[0] == '/value'
        flatNodes[1] == '/value/books'
        flatNodes[2] == '/value/books/book'
        flatNodes.size() == 14
    }


    def "Flatten leaf nodes"() {
        given:
        def xml = new XmlSlurper().parseText(xmlString)
        when:
        def leaves = xml.'**'.findAll { it.children().size() == 0 }

        leaves.each { node ->
            println "${path(node)} = ${node.text()}"
        }

        then:
        leaves.size() > 5
    }


    def "Flatten demo schema files"() {
        given:
        XmlParser parser = new XmlParser()
        def leftResource = getClass().getResourceAsStream('/templates/configsets/fusion-3.1.5/basic_configs/conf/managed-schema')
        def rightResource = getClass().getResourceAsStream('templates/configsets/fusion-4.2.6/_default/conf/managed-schema')
        def leftSchema = parser.parse(leftResource)
        def rightSchema = parser.parse(rightResource)

        when:
        def flatLeft = Helper.flattenPlus(leftMap)
        def flatRight = Helper.flattenPlus(rightMap)

        then:
        flatLeft.size() == 9
        flatRight.size() == 11
        flatLeft == flatRight
    }

}
