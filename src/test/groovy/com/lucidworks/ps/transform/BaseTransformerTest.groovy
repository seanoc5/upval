package com.lucidworks.ps.transform


import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/24/22, Friday
 * @description:
 */

class BaseTransformerTest extends Specification {
    static final String sampleLeafValue = 'simple leaf value in subMap of "one"'
    static final Map srcMap = [
            one    : [
                    aMap       : ['foo', 'bar'],
                    bSubMap    : [b1: 'b-one', b2: 'b-two', b3list: ['b3-1', 'b3-1']],
                    clistOfMaps: [[cSubmap1: ['one', 'two']], [cSubMap2: ['three', 'four']]],
                    oneSubLeaf : sampleLeafValue
            ],
            topLeaf: sampleLeafValue,
            two    : ['one', 'two', 'three',]
    ]
    // a sample value to use for setting/updating in rules, make this a constant to allow better condition testing/checking
    static final String newValue = 'new leaf value replacement'
    // one hard-coded slashy-path to work with for testing
    public static final String SLASHY_PATH_EX = '/one/oneSubLeaf'
    // create source map for better readability, this could be condensed...
    static final Map rulesMap = [
            copy  : ['.*': ''],
            set   : ["${SLASHY_PATH_EX}": newValue],
            remove: ['/two']]
    static final Rules rules = new Rules(rulesMap)

    // --------------------- TESTS --------------------
    def "basic eval-path 'get' expressions should work on default map"() {
        given:
        String slashyExpression = '/topLeaf'
        String rootExpression = 'ROOT.topLeaf'
        String subMapExpression = '/one/aMap'
        String subMapExpressionFirst = 'one.aMap[0]'
        String missingExpression = 'one.nothingHere'

        when:
        def slashyResult = BaseTransformer.evalObjectPathExpression(srcMap, slashyExpression)
        def rootResult = BaseTransformer.evalObjectPathExpression(srcMap, rootExpression)
        def subMapResult = BaseTransformer.evalObjectPathExpression(srcMap, subMapExpression)
        def subMapFirstResult = BaseTransformer.evalObjectPathExpression(srcMap, subMapExpressionFirst)
        def missingResult = BaseTransformer.evalObjectPathExpression(srcMap, missingExpression)

        then:
        slashyResult == sampleLeafValue
        rootResult == sampleLeafValue
        subMapResult == srcMap.one.aMap
        subMapFirstResult == srcMap.one.aMap[0]
        missingResult == null
    }


    def "transformer should be able to get values from destination Map when provided"() {
        given:
        Map destTemplate = srcMap               // use the srcMap as a sample template for the detination
        BaseTransformer transformer = new BaseTransformer(srcMap, rules, destTemplate, '/')
        String slashyTest = SLASHY_PATH_EX
        String dotTest = 'one.oneSubLeaf'       // sampleLeafValue
        String dotTestList = 'two[2]'           // 'three
        String dotTestListComplex = 'one.clistOfMaps[1].cSubMap2[0]'       // three  .csubMap2

        when:
        def slashyResult = transformer.getDestinationValue(slashyTest)
        def dotTestResult = transformer.getDestinationValue(dotTest)
        def dotTestListResult = transformer.getDestinationValue(dotTestList)
        def dotTestListComplexResult = transformer.getDestinationValue(dotTestListComplex)

        then:
        slashyResult == sampleLeafValue
        dotTestResult == sampleLeafValue
        dotTestListResult == 'three'
        dotTestListComplexResult == 'three'
    }

    def "transformer should be able to set various values in destination object of type map"() {
        given:
        Map destTemplate = [x: 'fubar']
        String testPath1 = SLASHY_PATH_EX
        String testPath2 = 'one.aMap[1]'
        String testPath3 = 'one.aMap[2]'
        BaseTransformer transformer = new BaseTransformer(srcMap, rules, destTemplate, '/')

        when:
        def result2 = transformer.setDestinationValue(testPath2, 'testPath2 updated')
        def result1 = transformer.setDestinationValue(testPath1, 'testPath1 updated')

        then:
        result1
        result2


    }

    def "GetNode"() {
        given:
        BaseTransformer transformer = new BaseTransformer(srcMap, rules, [:], '/')

        when:
        def nodeList = transformer.getNode('one.aMap')
        def nodeMap = transformer.getNode('one.bSubMap')

        then:
        nodeList instanceof List
        nodeMap instanceof Map

    }

/*
       def "GetNodeParent"() {
       given:
        BaseTransformer transformer = new BaseTransformer(srcMap, rules, [:], '/')

        when:

        then:

    }

    def "PerformCopyRules"() {
        given:
        BaseTransformer transformer = new BaseTransformer(srcMap, rules, [:], '/')

        when:

        then:

    }

    def "PerformSetRules"() {
        given:
        BaseTransformer transformer = new BaseTransformer(srcMap, rules, [:], '/')

        when:

        then:

    }

    def "PerformRemoveRules"() {
        given:
        BaseTransformer transformer = new BaseTransformer(srcMap, rules, [:], '/')

        when:

        then:

    }

    def "Transform"() {
        given:
        BaseTransformer transformer = new BaseTransformer(srcMap, rules, [:], '/')

        when:

        then:

    }
*/

}
