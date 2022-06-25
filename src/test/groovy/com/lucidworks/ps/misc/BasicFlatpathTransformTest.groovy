package com.lucidworks.ps.misc

import com.lucidworks.ps.upval.Helper
import spock.lang.Specification

import java.util.regex.Pattern

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/21/22, Tuesday
 * @description:
 */

/**
 * thinking through revised approach.
 */
class BasicFlatpathTransformTest extends Specification {
    Map srcMap = [a: [one: 1, two: 2], b: [3, 4]]

    def "check basic Map setting"() {
        when:
        String valToSet = 'new value here'
        srcMap.a.one = valToSet
        srcMap.a.one = valToSet

        then:
        srcMap.a.one == valToSet

    }

    def "flatten path should be as expected"() {
        when:
        def flatpaths = Helper.flattenWithLeafObject(srcMap)

        then:
        flatpaths.size() == 4
    }

    def "should be able to get elements by path regex pattern"() {
        when:
        def flatpaths = Helper.flattenWithLeafObject(srcMap)
        Pattern pathRegex = ~/\/a.*/
        def aItemPaths = flatpaths.findAll { String path, def val ->
            path ==~ pathRegex
        }

        then:
        flatpaths.size() == 4
        aItemPaths.size() == 2
    }

    def "should be able to get elements values"() {
        when:
        def flatpaths = Helper.flattenWithLeafObject(srcMap)
        Pattern pathRegex = ~/\/b\/.*/
        def aItemFlatPathMap = flatpaths.findAll { String path, def val ->
            path ==~ pathRegex
        }
        def aItemKeys = aItemFlatPathMap.keySet()

        then:
        flatpaths.size() == 4
        aItemFlatPathMap.size() == 2
        aItemKeys[0] == '/b/0'
        aItemKeys[1] == '/b/1'
        aItemFlatPathMap['/b/0'] == 3
        aItemFlatPathMap['/b/1'] == 4
    }

    def "should be able to SET elements values"() {
        when:
        def flatpaths = Helper.flattenWithLeafObject(srcMap)
        Map<String, Object> aoneVal = flatpaths['/a/one']
        aoneVal
        // setting an element in a list could be tricky?
        def b0Val = flatpaths['/b/0']


        then:
        aoneVal == 1
        atwoVal == 2
        b0Val == 3
        b1Val == 4
    }

}
