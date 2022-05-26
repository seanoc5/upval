package com.lucidworks.ps.upval

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Specification

/**
 * trying to test json pointer functionality (not much luck yet...)
 *
 * https://www.baeldung.com/json-pointer
 * https://javaee.github.io/jsonp/getting-started.html
 */
class JsonPointerPathCreateSpecification extends Specification {

    def "flatten basic map"() {
        given:
        Map map = [
                top1: [
                        middle1a: [bottom1a1: 'endleaf1', bottom1a2: 'endleaf2'],
                        middle1b: [bottom1b1: 'endleaf3']
                ],
                top2: [middle2a: [bottom2a1: 'endleaf1', bottom2a2: 'endleaf2'],
                       middle2b: [bottom2b1: 'endleaf3']
                ]
        ]

        when:
        DocumentContext context = JsonPath.parse(map)
        String allPath = '$.keys()'
        def foo = context.read(allPath)
        def result = Helper.flattenMap(map)

        then:
        result == ''
    }

}
