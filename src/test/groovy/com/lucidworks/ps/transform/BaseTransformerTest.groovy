package com.lucidworks.ps.transform

import com.lucidworks.ps.upval.Helper
import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/24/22, Friday
 * @description:
 */

class BaseTransformerTest extends Specification {
    Map srcMap = [
            one       : [
                    aMap       : [1, 2],
                    bSubMap    : [b1: 'b-one', b2: 'b-two', b3list: ['b3-1', 'b3-1']],
                    clistOfMaps: [[cSubmap1: [1, 2]], [csubMap2: [3, 4]]]],
            oneSubLeaf: 'simple leaf value in subMap of "one"',
            two       : [1, 2, 3]
    ]
    String newValue = 'new leaf value replacement'
    Map rulesMap = [
            copy  : ['.*':''],
            set   : ['/one/oneSubLeaf': newValue],
            remove: ['/two']]
    Rules rules = new Rules(rulesMap)

    def "BaseTransformer should be able to get certain values"() {
        given:
        Map destMap = [:]
        BaseTransformer transformer = new BaseTransformer(srcMap, rules, destMap)

        when:
        def flatmap = Helper.flattenWithLeafObject(srcMap)
        String path1 = '/one/aMap'
        def sourceA = transformer.getDestinationValue(path1)
        def destinationA = transformer.getDestinationValue(path1)

        then:
        flatmap.size() == 14
        sourceA instanceof Collection
        destinationA == null

    }

    def "GetDestinationValue"() {
    }

    def "GetNode"() {
    }

    def "GetNodeParent"() {
    }

    def "EvalObjectPathExpression"() {
    }

    def "PerformCopyRules"() {
    }

    def "PerformSetRules"() {
    }

    def "PerformRemoveRules"() {
    }

    def "Transform"() {
    }


}
