package com.lucidworks.ps.transform

import groovy.json.JsonOutput
import org.apache.commons.lang.StringEscapeUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/26/22, Sunday
 * @description:
 */

/**
 * test class for basic JsonSlurper object navigation (get nodes), and transformations (set nodes, escape/unescape string values, e.g. embedded javascript)
 */
class JsonObjectTest extends Specification {
    String formatedSource = '''function myFunc() {
\tvar a = "foo";
\tconsole.log(a);
}'''

    String singleLineSource = 'function myFunc() {\\n\\tvar a = \\"foo\\";\\n\\tconsole.log(a);\\n}'

    @Shared
    Map srcMap

    void setup() {
        srcMap = [
                a            : [one: 'one', two: 'two'],
                b            : ['three', 'four'],
                compositeList: ['comp1', 'comp2', [submapkey1: 'comp map 1 val']]
        ]
    }

    // ------------------ TESTS -------------------
//    def "UnEscapeSource"() {
//    }
//
//    def "EscapeSource"() {
//    }

    def "escaped source should be multiline and unescaped should not"(){
        expect:
        formatedSource.split('\n').size() == 4
        singleLineSource.split('\n').size() == 1
    }

    def "should get decoded javascript with commons-text directly (sanity check)"(){
        when:
        String unescaped = StringEscapeUtils.unescapeJavaScript(singleLineSource)
        List<String> singleLines = singleLineSource.split('\n')
        List<String> formatedLines = formatedSource.split('\n')

        then:
        singleLines.size() == 1
        formatedLines.size() == 4
        unescaped ==formatedSource

    }

    def "GetDecodedValue with JsonObject unescape call"() {
        when:
        String decoded = JsonObject.unEscapeSource(singleLineSource)

        then:
        decoded==formatedSource
    }

    def "JsonOutput should properly escape map with unescaped javascipt as a value"() {
        when:
        Map m = [foo:'bar', jsonLabel: formatedSource]
        String jsonString = JsonOutput.toJson(m)
        String prettyJson = JsonOutput.prettyPrint(jsonString)
        String expectedJsonString = '{"foo":"bar","jsonLabel":"' + singleLineSource + '"}'
        List<String> jsonLines = jsonString.split('\n')
        List<String> jsonLinesPretty = prettyJson.split('\n')
        List<String> expectedLines = expectedJsonString.split('\n')

        then:
        jsonString==expectedJsonString
        jsonLines.size()==1
        jsonLinesPretty.size()==4
        expectedLines.size()==1

    }

    // https://spockframework.org/spock/docs/1.3/all_in_one.html#_method_unrolling
    @Unroll
    def "basic get values sanity checks"() {
        when:
        def result = JsonObject.getObjectNodeValue(srcMap, path)

        then:
        result == checkValue

        where:
        path                           | separator | checkValue
        '/a/one'                       | '/'       | 'one'
        '/a/two'                       | '/'       | 'two'
        '/b/0/'                        | '/'       | 'three'
        '/componsiteList/2/submapkey1' | '/'       | null
    }

    @Unroll
    def "basic SET values sanity checks"() {
        given:
        Map newMapToAdd = [bizz: 1, buzz: 2]
        String valToSet = 'my new value here'

        when:
        def resultThree = JsonObject.setObjectNodeValue(srcMap, '/a/three', valToSet)
        def resultNewLeaf = JsonObject.setObjectNodeValue(srcMap, '/newTopLeaf', valToSet)
        def resultBizz = JsonObject.setObjectNodeValue(srcMap, '/a/four', newMapToAdd)

        then:
        resultNewLeaf == valToSet
        srcMap.newTopLeaf == valToSet

        resultThree == valToSet
        srcMap.a.three == valToSet
        resultBizz == '{bizz=1, buzz=2}'        // todo can we force returning the actual object rather than the toString() of the object?
        srcMap.a.four == newMapToAdd
    }

    def "basic SET values sanity checks with datatables"() {
        when:
        def result = JsonObject.setObjectNodeValue(srcMap, path, v, separator)

        then:
        checkValue == result

        where:
        path                           | separator | v           | checkValue
        '/newTopLeaf'                  | '/'       | "vnew"      | 'vnew'
        '/a'                           | '/'       | "vnew"      | 'vnew'
        '/a/two'                       | '/'       | 'new value' | 'new value'
        '/b/0/'                        | '/'       | 'new value' | 'new value'
        '/componsiteList/2/submapkey1' | '/'       | 'new value' | 'new value'
    }


}
