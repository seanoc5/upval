package com.lucidworks.ps.transform

import groovy.json.JsonOutput
import org.apache.commons.lang.StringEscapeUtils
import spock.lang.Specification
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

}
