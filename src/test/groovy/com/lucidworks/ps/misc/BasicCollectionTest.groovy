package com.lucidworks.ps.misc

import com.lucidworks.ps.upval.Helper
import groovy.xml.XmlParser
import spock.lang.Specification
/**
 * delete me -- first pass at comparing, moved
 * @deprecated
 * @see com.lucidworks.ps.misc.HelperSpecification
 */
class BasicCollectionTest extends Specification {
/*
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

*/

    def "simple demo of 'Flatten' with files"() {
        given:
        XmlParser parser = new XmlParser()
        InputStream leftResource = getClass().getResourceAsStream('/templates/configsets/fusion-3.1.5/basic_configs/conf/managed-schema')
        InputStream rightResource = getClass().getResourceAsStream('/templates/configsets/fusion-4.2.6/_default/conf/managed-schema')
        Node leftSchema = parser.parse(leftResource)
        Node rightSchema = parser.parse(rightResource)

        when:
        def flatLeft = Helper.flattenPlusMeta(LEFT_MAP)
        def flatRight = Helper.flattenPlusMeta(RIGHT_MAP)
        Set rightOnlyKeys = flatRight.keySet() - flatLeft.keySet()

        then:
        flatLeft.size() == 9
        flatRight.size() == 11
        rightOnlyKeys.size()==2
        rightOnlyKeys.toArray() == ['top2.3','top3.middle3.bottom2']
    }

}
