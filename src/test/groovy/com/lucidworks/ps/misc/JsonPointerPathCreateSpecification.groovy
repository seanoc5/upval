package com.lucidworks.ps.misc

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.lucidworks.ps.Helper
//import com.lucidworks.ps.upval.Helper
import spock.lang.Specification

/**
 * trying to test json pointer functionality (not much luck yet...)
 * see com.lucidworks.ps.mapping.ObjectTransformerJayway
 * @deprecated
 * https://www.baeldung.com/json-pointer
 * https://javaee.github.io/jsonp/getting-started.html
 */
class JsonPointerPathCreateSpecification extends Specification {

    def "flatten basic map"() {
        given:
        // intellij is interpreting groovy map as json below, showing errors, but it compiles and runs file, ignore warnings on single quotes
        Map m = [
                top1: [
                        middle1a: [bottom1a1: 'endleaf1', bottom1a2: 'endleaf2'],
                        middle1b: [bottom1b1: 'endleaf3']
                ],
                top2: [middle2a: [bottom2a1: 'endleaf1', bottom2a2: 'endleaf2'],
                       middle2b: [bottom2b1: 'endleaf3']
                ]
        ]

        when:
        DocumentContext context = JsonPath.parse(m)
        String allPath = '$.keys()'
        def foo = context.read(allPath)
        def result = Helper.flatten(m)

        then:
        result.size() == 6
        result[0] == 'top1.middle1a.bottom1a1'
        result[1] == 'top1.middle1a.bottom1a2'
        result[5] == 'top2.middle2b.bottom2b1'
    }

}
