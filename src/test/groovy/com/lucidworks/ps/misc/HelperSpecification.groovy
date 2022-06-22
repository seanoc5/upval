package com.lucidworks.ps.misc

import com.lucidworks.ps.upval.Helper
import spock.lang.Specification

class HelperSpecification extends Specification {
    Map map = [
            top1: [
                    list1   : [1, 2, 3],
                    middle1a: [bottom1a1: [subbottom1a1a: 'endleaf1'], bottom1a2: 'endleaf2',],
                    middle1b: [bottom1b1: 'endleaf3'],
                    middle1c: [[listMap1: 'a'], [listMap2: 'b']]
            ],
            top2: [
                    list1   : [1, 2, 3],
                    middle2a: [bottom2a1: 'endleaf1', bottom2a2: 'endleaf2'],
                    middle2b: [bottom2b1: 'endleaf3']
            ]
    ]

    def "should add missing elements"() {
        given:
        Map srcMap = [a: [one: 1, two: 2], b: [1, 2]]
        String missingPath = '/a/three/myMissingLeaf'
        def valToSet = 'fubar'

        when:
        def foo = Helper.getOrCreateJsonObjectNode(srcMap, missingPath, '/', valToSet)

        then:
        srcMap.a.three.myMissingLeaf == valToSet
    }


    def "simple flatten functionality"() {
        given:

        when:
        def flatties = Helper.flatten(map, 1)

        then:
        flatties instanceof List
        flatties.size() == 14
        flatties[0] == 'top1.middle1a.bottom1a1.subbottom1a1a'
        flatties[1] == 'top1.middle1a.bottom1a2'
    }

    def "flattenPlusObject"() {
        given:

        when:
        Map<String, Object> flatties = Helper.flattenPlusObject(map, 1)
        Set<String> paths = flatties.keySet()

        then:
        flatties instanceof Map
        paths.size() == 14
        paths[0] == 'top1.list1.0'
        flatties[paths[0]] == 1
        paths[1] == 'top1.list1.1'

        paths[13] == 'top2.middle2b.bottom2b1'
        flatties[paths[13]] == 'endleaf3'
    }


}
