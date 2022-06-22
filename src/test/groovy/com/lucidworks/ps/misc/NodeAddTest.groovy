package com.lucidworks.ps.misc

import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/21/22, Tuesday
 * @description:
 */

class NodeAddTest extends Specification{
/*
    def "should add a missing path node"() {
        given:
        Map srcMap = [a: [one: 1, two: 2], b: [1, 2]]
        String missingPath = '/a/three/myMissingLeaf'
        String valToSet = 'was I added?'
        List segments = missingPath.split('/')

        when:

    }
*/

    def "should add a missing node via groovy map.withDefault and inject"(){
        given:
        // https://stackoverflow.com/questions/56683855/groovy-map-populate-with-default-element
        def result = [1,2,3,4].inject([:].withDefault{[]}){ m, i ->
            m[ i%2==0 ? 'odd' : 'even' ] << i
            m
        }
        // => [even:[1, 3], odd:[2, 4]]
    }
}
