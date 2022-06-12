package com.lucidworks.ps.compare

import com.lucidworks.ps.upval.Helper
import groovy.xml.XmlParser
import spock.lang.Specification

/**
 * Less of a test suite, more of seeing how things can work...
 */
class BasicCollectionTest extends Specification {
    static final Map LEFT_MAP = [top1: 'first top node',
                                        top2: [1, 2, 3],
                                        top3: [
                                               middle1: 'top3:middle1',
                                               middle2: [4, 5, 6],
                                               middle3: [
                                                       bottom1: 'top3:middle3:bottom1'
                                               ]]]
    static final Map RIGHT_MAP = [top1: 'first top node',
                                         top2: [1, 2, 3, 4],
                                         top3: [
                                                middle1: 'top3:middle1 plus change',
                                                middle2: [4, 5, 6],
                                                middle3: [
                                                        bottom1: 'top3:middle3:bottom1',
                                                        bottom2: 'new bottom element']]]


    def "flattenPlusMeta"() {
        when:
        def leftFlatMap = Helper.flattenPlusMeta(LEFT_MAP)
        def rightFlatMap = Helper.flattenPlusMeta(RIGHT_MAP)

        then:
        leftFlatMap.size() == 9
        rightFlatMap.size() == 11
        leftFlatMap == rightFlatMap
    }

    def "flattenPlusObject"() {
        given:
        when:
        def flatLeft = Helper.flattenPlusObject(LEFT_MAP)
        def flatRight = Helper.flattenPlusObject(RIGHT_MAP)

        then:
        flatLeft.size() == 9
        flatRight.size() == 11
        flatLeft == flatRight
    }


    def "simple demo of 'Flatten' with files"() {
        given:
        XmlParser parser = new XmlParser()
        def leftResource = getClass().getResourceAsStream('/templates/configsets/fusion-3.1.5/basic_configs/conf/managed-schema')
        def rightResource = getClass().getResourceAsStream('templates/configsets/fusion-4.2.6/_default/conf/managed-schema')
        def leftSchema = parser.parse(leftResource)
        def rightSchema = parser.parse(rightResource)

        when:
        def flatLeft = Helper.flattenPlusMeta(LEFT_MAP)
        def flatRight = Helper.flattenPlusMeta(RIGHT_MAP)

        then:
        flatLeft.size() == 9
        flatRight.size() == 11
        flatLeft == flatRight
    }

}
