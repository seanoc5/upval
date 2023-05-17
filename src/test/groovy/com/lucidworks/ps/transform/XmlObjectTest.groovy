package com.lucidworks.ps.transform

import com.lucidworks.ps.misc.BasicXMLTest
import groovy.xml.XmlParser
import spock.lang.Specification
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   7/1/22, Friday
 * @description:
 */

class XmlObjectTest extends Specification {
//    @Shared
    Node XML_TEST_NODE

    void setup() {
        XML_TEST_NODE = new XmlParser().parseText(BasicXMLTest.XML_TEST_STRING)
        println("Xml parsed? $XML_TEST_NODE")
    }

    def "simple flatten functionality"() {
        when:
        def flatties = XmlObject.flattenXmlPath(XML_TEST_NODE, 1)

        then:
        flatties instanceof List
        flatties.size() == 15
        flatties[0] == '/response'
        flatties[1] == '/response/value'
        flatties[14] == '/response/value/books/book/author'
    }

    /**
     * todo -- implement
     * @return
     */
    def "flattenPlusObject should return map of path plus node"() {
        when:
        def flatties = XmlObject.flattenXmlPathPlusObject(XML_TEST_NODE, 1, '/')
//        Set<String> paths = flatties.keySet()

        then:
//        flatties instanceof Map
        flatties != null
        flatties instanceof Map
    }



}
