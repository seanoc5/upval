package com.lucidworks.ps.transform

import org.apache.log4j.Logger
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
    static final Logger log = Logger.getLogger(this.class.name);
    static final Map srcMap = [
            id          : 'LWF_Commerce',
            created     : '2022-07-15',
            one         : [
                    aMap       : ['foo', 'bar', 'LWFX-123'],
                    bSubMap    : [b1: 'b-one', b2: 'b-two', b3list: ['b3-1', 'b3-1']],
                    clistOfMaps: [[cSubmap1: ['one', 'two']], [cSubMap2: ['three', 'four']]],
                    oneSubLeaf : 'Simple leaf',
                    id         : 'LWF_test',
                    created    : '2020-01-01',
            ],
            two         : ['one', 'two', 'three',],
            threeTopLeaf: 'Simple leaf',
    ]


    def "find all entries with value"() {
        given:
        Map destMap = srcMap.clone()
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, destMap)

        when:
        def srcMatchesOneAMap = transformer.findAllItemsMatching('/one/aMap', '', transformer.srcFlatpaths)
        def srcMatchesFoo = transformer.findAllItemsMatching('/one/.*', 'foo', transformer.srcFlatpaths)
        def srcMatchesFooBar = transformer.findAllItemsMatching('/one/.*', /(foo|bar)/, transformer.srcFlatpaths)
        def srcMatchesBars = transformer.findAllItemsMatching('.*Map.*', /(b-.*)/, transformer.srcFlatpaths)


        then:
        srcMatchesFoo.size() == 1
        srcMatchesFoo.keySet()[0] == '/one/aMap/0'
        srcMatchesFoo['/one/aMap/0'] == 'foo'

        srcMatchesFooBar.size() == 2        // problem with matching non-leaf objects, this is failing and shows future work...
        srcMatchesFooBar.keySet()[1] == '/one/aMap/1'
        srcMatchesFooBar['/one/aMap/1'] == 'bar'

        srcMatchesBars.size() == 2
        srcMatchesBars.keySet()[0] == '/one/bSubMap/b1'
        srcMatchesBars.keySet()[1] == '/one/bSubMap/b2'
        srcMatchesBars['/one/bSubMap/b1'] == 'b-one'
        srcMatchesBars['/one/bSubMap/b2'] == 'b-two'

    }

    def "copy-rule basic copy test"() {
        given:
        def rules = [copy: [[sourcePath: '.*', sourceItemPattern: 'LWF.*'],],]
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, srcMap)

        when:
        def results = transformer.transform(rules)

        then:
        //todo more testing here...
        results instanceof Map
        transformer.destinationObject.id == 'LWF_Commerce'
        transformer.destinationObject.one.id == 'LWF_test'
        transformer.destinationObject.one.aMap.size() == 3
        transformer.destinationObject.one.aMap[2] == 'LWFX-123'
    }


    def "copy-rule string replace transform test"() {
        given:
        def rules = [
                copy: [
                        [sourcePath: '.*',
                         sourceItemPattern: 'LWF_',
                         destinationPath: '',
                         destinationExpression: 'Acme_'],
                ],
        ]
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, srcMap)
        Map destMap = transformer.destinationObject

        when:
        def results = transformer.transform(rules)

        then:
        results instanceof Map
        destMap.id == 'Acme_Commerce'
        destMap.one.id == 'Acme_test'
    }

    /**
     * todo -- implement set rules, currently just a placeholder...
     * @return
     */
    def "regex set-rule transform test"() {
        given:
        def rules = [
                set: [destinationPath: /.*DC_Large/],
        ]
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, srcMap)
        Map destMap = transformer.destinationObject

        when:
        def results = transformer.transform(rules)

        then:
        destMap.id == 'Acme_Commerce'
        destMap.one.id == 'Acme_test'
    }

    def "remove-rule should remove items based on rules"() {
        given:
        def rules = [
                remove: [
                        [pathPattern: /.*bSub.*/, valuePattern: ''],
                        [pathPattern: /.*/, valuePattern: 'one'],
                ],
        ]
        Map destMap = srcMap.clone()
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, destMap)

        when:
        def results = transformer.transform(rules)
        def newFlatties = JsonObject.flattenWithLeafObject(transformer.destinationObject)

        then:
        newFlatties.size() == 14
    }


    /**
     * todo -- implement remove rules, currently just a placeholder...
     * @return
     */
    def "remove-rule should remove item based on simple rule"() {
        given:
        def rules = [
                remove: [
                        [pathPattern: "/one/aMap", valuePattern: ''],
                ],
        ]
        Map destMap = srcMap.clone()
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, destMap)

        when:
        def results = transformer.transform(rules)
        def newFlatties = JsonObject.flattenWithLeafObject(transformer.destinationObject)

        then:
        // dest map is shallow clone, so in these tests source is also modified (consider doing deep copy to leave source map untouched)
        transformer.srcFlatpaths.size() == 20           // this is 14 when using 'old' flatmaps with only leafnodes, and 20 with 'new' flatmaps that include paths for non-leaf nodes
        newFlatties.size() == 17
        destMap.one.aMap == null
        destMap.one.bSubMap.keySet().toList() == [ 'b1', 'b2', 'b3list']
    }


    def "remove-rule should remove items based on rules in larger set"() {
        given:
        def rules = [
                remove: [
                        [pathPattern: /.*(\/updates\/).*/, valuePattern: '.*'],
                        [pathPattern: /.*(\/createdAt\/).*/, valuePattern: '.*'],
                        [pathPattern: /.*/, valuePattern: '10000'],
                ],
        ]
        Map destMap = srcMap.clone()
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, destMap)

        when:
        def results = transformer.transform(rules)

        then:
        destMap.id == 'Acme_Commerce'
        destMap.one.id == 'Acme_test'
    }

    def "orderIndexKeysDecreasing should sort flatPaths with collection index items in reverse order"() {
        given:
        Map map = [
                a: [foo: ['one', 'two'],],
                b: ['b1', 'b2', 'b3'],
                c: ['c1', 'c2', 'c3', 'c4',],
                d: [d1:'d-leaf', d2:'d2', d3:[9,9,9,]]
        ]
        def flatties = JsonObject.flattenWithLeafObject(map)

        when:
        Set<String> myOrderedSet = JsonObject.orderIndexKeysDecreasing(flatties)

        then:
        myOrderedSet.size() == 14
        myOrderedSet.getAt(0) == '/c/3'
        myOrderedSet.getAt(1) == '/b/2'
        myOrderedSet.getAt(3) == '/d/d3/2'
        myOrderedSet.getAt(8) == '/a/foo/0'
        myOrderedSet.getAt(13) == '/d/d2'

    }

}




