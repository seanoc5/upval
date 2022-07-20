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

    def "Basic copy test"() {
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


    def "regex replace transform test"() {
        given:
        def rules = [
                copy: [
                        [sourcePath: '.*', sourceItemPattern: '~LWF_', destinationPath: '', destinationExpression: 'Acme_'],     // trying `~` syntax to say search/replace with
                ],
//                set   : [destinationPath: /.*DC_Large/],
//                remove: [/.*(created|modified|lastUpdated)/]
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
    def "regex set transform test"() {
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

    def "should remove items based on rules"() {
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
//        Map destMap = transformer.destinationObject

        then:
        transformer.srcFlatpaths.size() == 20
        transformer.srcFlatpaths['/one/clistOfMaps/0/cSubmap1/0'] == 'one'
        newFlatties.size() == 14

        newFlatties['/one/clistOfMaps/0/cSubmap1/0'] == 'two'       // Note: if we remove in 'natural' order, it will be 0, then 2 and index 2 (third item) will not exist because the first element will be gone, throwing an error, if done 'intelligently' we will remove in decreasing index order, idex 2, then 0, left with what was index 1, value 'two'
        destMap.one.clistOfMaps[0].cSubmap1.size() == 1
        destMap.one.clistOfMaps[0].cSubmap1[0] == 'two'

    }

    def "should remove collections items with potentially dangerous indexing- 1 and 3"() {
        given:
        def rules = [
                remove: [
                        [pathPattern: /\/two\/(0|2)/, valuePattern: ''],
                ],
        ]
        Map destMap = srcMap.clone()
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, destMap)

        when:
        int origSize = srcMap.two.size()
        def results = transformer.transform(rules)
        def newFlatties = JsonObject.flattenWithLeafObject(transformer.destinationObject)

        then:
        transformer.srcFlatpaths.size() == 20
        newFlatties.size() == 18
        origSize == 3
        destMap.two.size() == 1
        destMap.two[0] == 'two'

    }


    def "should remove items based on rules in larger set"() {
        given:
        File idxpJson = new File(getClass().getResource('/apps/indexPipeline.1.json').toURI())

        def rules = [
                remove: [
                        [pathPattern: /.*(\/updates\/).*/, valuePattern: '.*'],
                        [pathPattern: /.*(\/createdAt\/).*/, valuePattern: '.*'],
                        [pathPattern: /.*/, valuePattern: '~10000'],
                ],
        ]
        Map srcMap = JsonObject.parseJson(idxpJson)
        Map destMap = JsonObject.parseJson(idxpJson)            // todo -- revisit destMap... is there a better approach? just have all rules start with explicitly deep-copying values from source...???
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap, destMap)

        when:
        def results = transformer.transform(rules)

        then:
        destMap.updates == null
    }

    def "should sort flatPaths with collection index items in reverse order"() {
        given:
        Map map = [
                a: [foo: ['one', 'two'],],
                b: ['b1', 'b2', 'b3'],
                c: ['c1', 'c2', 'c3', 'c4',],
                d: [d1: 'd-leaf', d2: 'd2', d3: [9, 9, 9,]]
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




