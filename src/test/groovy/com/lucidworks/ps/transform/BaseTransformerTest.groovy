package com.lucidworks.ps.transform


import spock.lang.Specification
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/24/22, Friday
 * @description:
 */

class BaseTransformerTest extends Specification {
    Map srcMap = [
            one: [
                    aMap: [1, 2],
                    bSubMap: [b1: 'b-one', b2: 'b-two', b3list: ['b3-1', 'b3-1']],
                    clistOfMaps: [[cSubmap1:[1,2]], [csubMap2:[3,4]]]],
            two:[1,2,3]
    ]
    BaseTransformer transformer = new BaseTransformer()

    def "GetSourceValue"() {
        when
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
