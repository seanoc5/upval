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

    def "should add missing map elements"() {
        given:
        Map srcMap = [a: [one: 1, two: 2], b: [1, 2]]
        String existingPath = '/a/two'
        String missingPath = '/a/three/myMissingLeaf'
        def valToUpdate = 'my UPDATE Value'
        def valToCreate = 'my NEW value'

        when:
        def missingElement = Helper.getObjectNodeValue(srcMap, missingPath, '/')
        def existingElement = Helper.getObjectNodeValue(srcMap, existingPath, '/')
        def originalExistingElementValue = existingElement.toString()
        def updatedElement = Helper.setJsonObjectNode(srcMap, existingPath, '/', valToUpdate)
        def addedElement = Helper.setJsonObjectNode(srcMap, missingPath, '/', valToCreate)

        then:
        missingElement == null
        originalExistingElementValue == '2'
        updatedElement['two'] == valToUpdate
        addedElement['myMissingLeaf'] == valToCreate
        srcMap.a.two == valToUpdate
        srcMap.a.three.myMissingLeaf == valToCreate
    }

    def "should add missing list elements"() {
        given:
        Map srcMap = [b: ['first', 'second']]
        String existingPath = '/b/1'
        String missingPath = '/b/2'
        def valToUpdate = 'my UPDATE Value'
        def valToCreate = 'my NEW value'

        when:
        def missingElement = Helper.getObjectNodeValue(srcMap, missingPath, '/')
        def existingElement = Helper.getObjectNodeValue(srcMap, existingPath, '/')
        def originalExistingElementValue = existingElement.toString()
        def updatedElement = Helper.setJsonObjectNode(srcMap, existingPath, '/', valToUpdate)
        def addedElement = Helper.setJsonObjectNode(srcMap, missingPath, '/', valToCreate)

        then:
        missingElement == null
        originalExistingElementValue == 'secoond'
        updatedElement['two'] == valToUpdate
        addedElement['myMissingLeaf'] == valToCreate
        srcMap.a.two == valToUpdate
        srcMap.a.three.myMissingLeaf == valToCreate
    }


    def "simple flatten functionality"() {
        when:
        def flatties = Helper.flatten(map, 1)

        then:
        flatties instanceof List
        flatties.size() == 14
        flatties[0] == 'top1.middle1a.bottom1a1.subbottom1a1a'
        flatties[1] == 'top1.middle1a.bottom1a2'
    }

    def "flattenPlusObject"() {
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
