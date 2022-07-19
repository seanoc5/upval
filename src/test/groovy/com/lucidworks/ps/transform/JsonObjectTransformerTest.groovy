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
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap)
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
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap)
        Map destMap = transformer.destinationObject

        when:
        def results = transformer.transform(rules)

        then:
        destMap.id == 'Acme_Commerce'
        destMap.one.id == 'Acme_test'
    }

    /**
     * todo -- implement remove rules, currently just a placeholder...
     * @return
     */
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

        then:
        destMap.id == 'Acme_Commerce'
        destMap.one.id == 'Acme_test'
    }


    def "should remove items based on rules in larger set"() {
        given:
        def rules = [
                remove: [
                        [pathPattern: /.*(\/updates\/).*/, valuePattern: '.*'],
                        [pathPattern: /.*(\/createdAt\/).*/, valuePattern: '.*'],
                        [pathPattern: /.*/, valuePattern: '~10000'],
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

}
