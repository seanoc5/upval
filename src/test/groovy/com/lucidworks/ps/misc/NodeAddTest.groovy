package com.lucidworks.ps.misc

import com.lucidworks.ps.transform.JsonObject
import org.apache.log4j.Logger
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/21/22, Tuesday
 * @description:
 */

class NodeAddTest extends Specification {
    static final Logger log = Logger.getLogger(this.class.name);
    static final String valToSet = 'was I added?'
    static final String aOneVal = 'one'
    static final String b1Val = 'three'
    static final String aOnePath = '/a/one'
    static final String missingPath = '/a/one/three'

    @Shared
    Map srcMap

    void setup() {
        srcMap = [
                a             : [one: aOneVal, two: 'two'],
                b             : [b1Val, 'four'],
                compositeList: ['comp1', 'comp2', [submapkey1: 'comp map 1 val']]
        ]

    }

// https://spockframework.org/spock/docs/1.3/all_in_one.html#_method_unrolling
    @Unroll
    def "basic get values sanity checks"() {
        when:
        def result = JsonObject.getObjectNodeValue(srcMap, path)

        then:
        result == checkValue

        where:
        path                           | separator | checkValue
        aOnePath                       | '/'       | aOneVal
        '/a/two'                       | '/'       | 'two'
        '/b/0/'                        | '/'       | b1Val
        '/componsiteList/2/submapkey1' | '/'       | 'comp map 1 val'
    }

    @Unroll
    def "basic SET values sanity checks"() {
        given:
        Map newMapToAdd = [bizz: 1, buzz: 2]

        when:
        def resultThree = JsonObject.setObjectNodeValue(srcMap, '/a/three', valToSet)
        def resultNewLeaf = JsonObject.setObjectNodeValue(srcMap, '/newTopLeaf', valToSet)
//        def resultBizz = JsonObject.setObjectNodeValue(srcMap, '/a/four', newMapToAdd)

        then:
        resultNewLeaf == valToSet
        srcMap.newTopLeaf == valToSet

        resultThree == valToSet
        srcMap.a.three == valToSet
//        resultBizz == '{}'
//        srcMap.a.four == newMapToAdd
    }

    def "basic SET values sanity checks with datatables"() {
        given:
        String vnew = 'new set val'

        when:
        def result = JsonObject.setObjectNodeValue(srcMap, path, v, separator)

        then:
        checkValue == result

        where:
        path                           | separator | v           | checkValue
        '/newTopLeaf'                  | '/'       | "vnew"      | 'vnew'
        '/a'                           | '/'       | "vnew"      | 'vnew'
        '/a/two'                       | '/'       | 'new value' | 'new value'
        '/b/0/'                        | '/'       | 'new value' | 'new value'
        '/componsiteList/2/submapkey1' | '/'       | 'new value' | 'new value'
    }

}
