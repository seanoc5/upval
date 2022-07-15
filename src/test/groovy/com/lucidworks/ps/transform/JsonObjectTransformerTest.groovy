package com.lucidworks.ps.transform


import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/24/22, Friday
 * @description:
 */

/**
 * @ deprecated ?? revisit to see if there is anything current/useful here...
 */
class JsonObjectTransformerTest extends Specification {
    static final String sampleLeafValue = 'simple leaf value in subMap of "one"'
    static final Map srcMap = [
            one         : [
                    aMap       : ['foo', 'bar'],
                    bSubMap    : [b1: 'b-one', b2: 'b-two', b3list: ['b3-1', 'b3-1']],
                    clistOfMaps: [[cSubmap1: ['one', 'two']], [cSubMap2: ['three', 'four']]],
                    oneSubLeaf : sampleLeafValue
            ],
            two         : ['one', 'two', 'three',],
            threeTopLeaf: sampleLeafValue,
    ]
    // a sample value to use for setting/updating in rules, make this a constant to allow better condition testing/checking
//    static final String newValue = 'new leaf value replacement'
    // one hard-coded slashy-path to work with for testing
//    public static final String oneSubLeafPath = '/one/oneSubLeaf'
    // note: sourcePathPattern, sourceItemPattern, sourceValueExpression(regex), destinationPath
    // create source map for better readability, this could be condensed...
//    static final Map rulesMap = [
//            copy  : [[sourcePath:'(.*Leaf)', sourceItemPattern:'(simple)(.*)', destinationPath:'', destinationValue:'Complex $1']],
//            set   : ["${oneSubLeafPath}": newValue],
//            remove: ['/two']]
//    static final Rules rules = new Rules(rulesMap)

    // --------------------- TESTS --------------------
    def "find all entries with value"() {
        given:
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap)

        when:
        def srcMatchesFoo = transformer.findAllItemsMatching('/one/.*', 'foo', transformer.srcFlatpaths)
        def srcMatchesFooBar = transformer.findAllItemsMatching('/one/.*', /(foo|bar)/, transformer.srcFlatpaths)
        def srcMatchesBars = transformer.findAllItemsMatching('.*Map.*', /(b-.*)/, transformer.srcFlatpaths)


        then:
        srcMatchesFoo.size() == 1
        srcMatchesFoo.keySet()[0] == '/one/aMap/0'
        srcMatchesFoo['/one/aMap/0'] == 'foo'

        srcMatchesFooBar.size() == 2
        srcMatchesFooBar.keySet()[1] == '/one/aMap/1'
        srcMatchesFooBar['/one/aMap/1'] == 'bar'

        srcMatchesBars.size() == 2
        srcMatchesBars.keySet()[0] == '/one/bSubMap/b1'
        srcMatchesBars.keySet()[1] == '/one/bSubMap/b2'
        srcMatchesBars['/one/bSubMap/b1'] == 'b-one'
        srcMatchesBars['/one/bSubMap/b2'] == 'b-two'

    }

    def "Basic transform test"() {
        given:
        def rules = [
                copy  : [
//                        [sourcePath:'/one/.*', ],     // todo -- fix JsonObject setting missing items
//                        [sourcePath:'/two/0', ],
                        [sourcePath:'(.*Leaf)', sourceItemPattern:'(simple)(.*)', destinationPath:'', destinationValue:'Complex $1'],
                        [sourcePath:'(.*Leaf)', sourceItemPattern:'(simple)(.*)', destinationPath:'', destinationValue:'Complex $1']
                ],
                set   : [destinationPath: /.*DC_Large/],
                remove: [/.*(created|modified|lastUpdated)/]
        ]
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap)

        when:
        def results = transformer.transform(rules)

        then:
        results instanceof Map

    }


    /**
     * todo -- revisit? outdated/broken??
     * @return
     */
/*
    def "basic eval-path 'get' expressions should work on default map"() {
        given:
        String slashyExpression = '/threeTopLeaf'
        String subMapExpression = '/one/aMap'
        String subMapExpressionFirst = '/one/aMap/0'
        String missingExpression = 'one.nothingHere'

        when:
        def slashyResult = JsonObjectTransformer.evalObjectPathExpression(srcMap, slashyExpression)
        def rootResult = JsonObjectTransformer.evalObjectPathExpression(srcMap, rootExpression)
        def subMapResult = JsonObjectTransformer.evalObjectPathExpression(srcMap, subMapExpression)
        def subMapFirstResult = JsonObjectTransformer.evalObjectPathExpression(srcMap, subMapExpressionFirst)
        def missingResult = JsonObjectTransformer.evalObjectPathExpression(srcMap, missingExpression)

        then:
        slashyResult == sampleLeafValue
        rootResult == sampleLeafValue
        subMapResult == srcMap.one.aMap
        subMapFirstResult == srcMap.one.aMap[0]
        missingResult == null
    }
*/


/*
    def "transformer should be able to get values from destination Map when provided"() {
        given:
        Map destTemplate = srcMap               // use the srcMap as a sample template for the detination
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, destTemplate, '/')
        String slashyTest = oneSubLeafPath
        String dotTest = 'one.oneSubLeaf'       // sampleLeafValue
        String dotTestList = 'two[2]'           // 'three
        String dotTestListComplex = 'one.clistOfMaps[1].cSubMap2[0]'       // three  .csubMap2
        transformer.transform(rules)

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
*/

/*
    def "transformer should be able to set various values in destination object of type map"() {
        given:
        Map destTemplate = [x: 'fubar']
        String testPath1 = oneSubLeafPath
        String testPath2 = 'one.aMap[1]'
        String testPath3 = 'one.aMap[2]'
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, destTemplate, '/')

        when:
        def result2 = transformer.setDestinationValue(testPath2, 'testPath2 updated')
        def result1 = transformer.setDestinationValue(testPath1, 'testPath1 updated')

        then:
        result1
        result2


    }
*/

}
