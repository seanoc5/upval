package com.lucidworks.ps.transform


import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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


    def "escaped source should be multiline and unescaped should not"() {
        expect:
        formatedSource.split('\n').size() == 4
        singleLineSource.split('\n').size() == 1
    }

    def "should get decoded javascript with commons-text directly (sanity check)"() {
        when:
        String unescaped = StringEscapeUtils.unescapeJavaScript(singleLineSource)
        List<String> singleLines = singleLineSource.split('\n')
        List<String> formatedLines = formatedSource.split('\n')

        then:
        singleLines.size() == 1
        formatedLines.size() == 4
        unescaped == formatedSource

    }

    def "GetDecodedValue with JsonObject unescape call"() {
        when:
        String decoded = JsonObject.unEscapeSource(singleLineSource)

        then:
        decoded == formatedSource
    }

    def "JsonOutput should properly escape map with unescaped javascipt as a value"() {
        when:
        Map m = [foo: 'bar', jsonLabel: formatedSource]
        String jsonString = JsonOutput.toJson(m)
        String prettyJson = JsonOutput.prettyPrint(jsonString)
        String expectedJsonString = '{"foo":"bar","jsonLabel":"' + singleLineSource + '"}'
        List<String> jsonLines = jsonString.split('\n')
        List<String> jsonLinesPretty = prettyJson.split('\n')
        List<String> expectedLines = expectedJsonString.split('\n')

        then:
        jsonString == expectedJsonString
        jsonLines.size() == 1
        jsonLinesPretty.size() == 4
        expectedLines.size() == 1

    }

    // https://spockframework.org/spock/docs/1.3/all_in_one.html#_method_unrolling
    @Unroll
    def "basic get values sanity checks"() {
        when:
        def result = JsonObject.getObjectNodeValue(srcMap, path)

        then:
        result == checkValue

        where:
        path                          | separator | checkValue
        '/a/one'                      | '/'       | 'one'
        '/a/two'                      | '/'       | 'two'
        '/b/0/'                       | '/'       | 'three'
        '/compositeList/2/submapkey1' | '/'       | 'comp map 1 val'
    }

    @Unroll
    def "basic SET values sanity checks"() {
        given:
        Map newMapToAdd = [bizz: 1, buzz: 2]            // testing being able to add a new structure, not just a leaf node (primative value)
        String valToSet = 'my new value here'           // using a variable (string) for setting new values, makes testig easier/more consisten
        // check if element exists initially, same as: srcMap['a']['three'] -- GPath shorthand
        // element does not exist initially
        assert srcMap.a.three == null
        assert srcMap.b[0] == 'three'

        when:
        // set a List element by index:
        Map<String, Object> b0 = JsonObject.setObjectNodeValue(srcMap, '/b/0', valToSet)
        def b0Key = b0.keySet()[0]

        // add a new element (map entry) to existing parent (sub-map: a)
        Map<String, Object> resultThree = JsonObject.setObjectNodeValue(srcMap, '/a/three', valToSet)       // see upval JsonObject for 'slashy' string GPath/JsonPath alternative

        // create new 'top' leaf node
        Map<String, Object> resultNewLeaf = JsonObject.setObjectNodeValue(srcMap, '/newTopLeaf', valToSet)

        // create a new map (not simple primative value) in a new element (map entry)
        Map<String, Object> resultNewFourSubMap = JsonObject.setObjectNodeValue(srcMap, '/a/four', newMapToAdd)

        // check navigating through maps and lists
        Map<String, Object> resulCompositekey1 = JsonObject.setObjectNodeValue(srcMap, '/compositeList/2/submapkey1', valToSet)

        then:
        b0.get(b0Key) == valToSet                      // successfully set list element by index
        resultNewLeaf.keySet()[0] == '/newTopLeaf'           // added a new leaf node: to parent element(map) `a`
        resultNewLeaf.get(resultNewLeaf.keySet()[0]) == valToSet

        srcMap.newTopLeaf == valToSet

        resultThree.keySet()[0] == '/a/three'
        resultThree.get(resultThree.keySet()[0]) == valToSet
        srcMap.a.three == valToSet          // element `a.three` does exist now
        resultNewFourSubMap.toString() == "[/a/four:{bizz=1, buzz=2}]"      // todo can we force returning the actual object rather than the toString() of the object?
        srcMap.a.four.bizz == 1
        srcMap.a.four.buzz == 2
    }

    def "basic SET values sanity checks with datatables"() {
        when:
        Map<String, Object> result = JsonObject.setObjectNodeValue(srcMap, path, v, separator)

        then:
        checkValue == result.get(result.keySet()[0])

        where:
        path                           | separator | v           | checkValue
        '/newTopLeaf'                  | '/'       | "vnew"      | 'vnew'
        '/a'                           | '/'       | "vnew"      | 'vnew'
        '/a/two'                       | '/'       | 'new value' | 'new value'
        '/b/0/'                        | '/'       | 'new value' | 'new value'
        '/componsiteList/2/submapkey1' | '/'       | 'new value' | 'new value'
    }


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


    def "flatten a map and return path PlusObject"() {
        when:
        Map<String, Object> flatties = JsonObject.flattenWithLeafObject(map, 1, '/', '/')
        Set<String> paths = flatties.keySet()

        then:
        flatties instanceof Map
        paths.size() == 14
        paths[0] == '/top1/list1/0'
        flatties[paths[0]] == 1
        paths[1] == '/top1/list1/1'

        paths[13] == '/top2/middle2b/bottom2b1'
        flatties[paths[13]] == 'endleaf3'
    }


    String testJson = '''
{
    "id": "test_typeahead_inclusion_list",
    "created": "2021-04-26T23:05:53.940Z",
    "modified": "2021-04-26T23:05:53.940Z",
    "connector": "lucid.fileupload",
    "type": "fileupload",
    "pipeline": "test_typeahead_IPL_v4",
    "parserId": "_system",
    "properties": {
        "collection": "test_typeahead_v4",
        "fileId": "typeahead/Typeahead_inclusion_list.csv",
        "mediaType": "text/csv",
        "foo":["a", "b", "c"]
    }
}
'''

    def "parse and flatten a json String and return path PlusObject"() {
        when:
        JsonSlurper slurper = new JsonSlurper()
        def json = slurper.parseText(testJson)
        Map<String, Object> flatties = JsonObject.flattenWithLeafObject(json, 1, '/', '/')
        Set<String> paths = flatties.keySet()

        then:
        flatties instanceof Map
        paths.size() == 13

        paths[0] == '/id'
        flatties[paths[0]] == 'test_typeahead_inclusion_list'

        paths[7] == '/properties/collection'

        paths[10] == '/properties/foo/0'
        flatties[paths[10]] == 'a'
    }


    String testCollectionJson = '''
{
  "objects" : {
    "collections" : [ {
      "id" : "test",
      "createdAt" : "2020-05-12T00:12:46.499Z",
      "searchClusterId" : "default",
      "commitWithin" : 10000,
      "solrParams" : {
        "name" : "test",
        "numShards" : 1,
        "replicationFactor" : 1
      },
      "type" : "DATA",
      "metadata" : { }
    }, {
      "id" : "test_job_reports",
      "createdAt" : "2020-05-12T00:12:46.507Z",
      "searchClusterId" : "default",
      "commitWithin" : 10000,
      "solrParams" : {
        "name" : "test_job_reports",
        "numShards" : 1,
        "replicationFactor" : 1
      },
      "type" : "JOB_REPORTS",
      "relatedCollectionId" : "test",
      "metadata" : { }
    } ]
    }
}'''

    def "parse and flatten a json Collection-fragment and return path PlusObject"() {
        when:
        JsonSlurper slurper = new JsonSlurper()
        def json = slurper.parseText(testCollectionJson)
        Map<String, Object> flatties = JsonObject.flattenWithLeafObject(json, 1, '/', '/')
        Set<String> paths = flatties.keySet()

        then:
        flatties instanceof Map
        paths.size() == 17

        paths[0] == '/objects/collections/0/id'
        flatties[paths[0]] == 'test'

        paths[4] == '/objects/collections/0/solrParams/name'
        flatties[paths[4]] == 'test'

        paths[15] == '/objects/collections/1/type'
        flatties[paths[15]] == 'JOB_REPORTS'
    }

    def "parse and flatten a test objects.json partial file and return path PlusObject"() {
        when:
        File objectsFile = new File(getClass().getResource('/apps/objects.test.f5.partial.json').toURI())

        JsonSlurper slurper = new JsonSlurper()
        def json = slurper.parse(objectsFile)
        Map<String, Object> flatties = JsonObject.flattenWithLeafObject(json, 1, '/', '/')
        Set<String> paths = flatties.keySet()

        then:
        flatties instanceof Map
        paths.size() == 5107

        paths[0] == '/objects/collections/0/id'
        flatties[paths[0]] == 'test'

        paths[15] == '/objects/collections/1/type'
        flatties[paths[15]] == 'JOB_REPORTS'
    }


    def "should add missing branches and leafs"() {
        given:
        Map map = [:]

        when:
        def c5three = JsonObject.setObjectNodeValue(map, '/c/5/three', 'c-5-three')
        def c0Two = JsonObject.setObjectNodeValue(map, '/c/0/two', 'c-0-two')
        def bSubOne = JsonObject.setObjectNodeValue(map, '/b/one', 'b-one')
        def a = JsonObject.setObjectNodeValue(map, '/a', 'a-value')

        then:
        map.c[5].three == 'c-5-three'

        c0Two.keySet()[0] == '/c/0/two'
        c0Two.get(c0Two.keySet()[0]) == 'c-0-two'
        map.c[0].two == 'c-0-two'

        bSubOne.keySet()[0] == '/b/one'
        bSubOne.get(bSubOne.keySet()[0]) == 'b-one'
        map.b.one == 'b-one'

        a.keySet()[0] == '/a'
        a.get(a.keySet()[0]) == 'a-value'
        map.a == 'a-value'

    }

}


