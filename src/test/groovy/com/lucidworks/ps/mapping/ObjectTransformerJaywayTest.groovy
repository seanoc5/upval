package com.lucidworks.ps.mapping

import misc.ObjectTransformerJayway
import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/21/22, Tuesday
 * @description:
 */

class ObjectTransformerJaywayTest extends Specification {
    def "should naviate JsonSlurped map"() {
        given:
        Map srcMap = [a: [one: 1, two: 2], b: [1, 2]]
        Map rules = [
                // list of rules for setting 'defined' value (i.e. string, number, boolean,... hopefully an evaluated expression...?)
                set   : ['/type':'lucidworks.ldap'],
                // list of rules for copying from source to destination (aka: left to right)
                copy  : ['/id':'/id'],
                // list of rules to remove value from destination (helpful if copy is a wildcard, and we need to remove a few sub-items...)
                remove: ['/diagnosticLogging'],
        ]


        when:
//        ObjectTransformerJayway transformer = new ObjectTransformerJayway(srcMap, rules)
        def result = ObjectTransformerJayway.transform(srcMap, rules)

        then:
        // todo -- replace me with real test...
        result.size() > 0
    }

    def "EvaluateValue"() {
    }
}
