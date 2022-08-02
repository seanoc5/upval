package com.lucidworks.ps.transform

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringEscapeUtils
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

    Map srcMap          // reset for each test by 'setup' method, need it to be annotated with Shared???

    /**
     * using setup to refresh the source map for each test (is there a better approach??)
     */
    void setup() {
        // todo -- refactor, and make one all-purpose source map (see other map defined below)
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

    @Unroll
    def "findItems() get values sanity checks"() {
        when:
        def jsonObject = new JsonObject(srcMap)
        def result = jsonObject.findItems(path, valuePattern)

        then:
        result.toString() == checkValue.toString()

        where:
        path        | valuePattern | checkValue
        '/a'        | ''           | ['/a': ['one': 'one', 'two': 'two']]
        '/a/one'    | ''           | ['/a/one': 'one']
        ~'/a/(two)' | ''           | ['/a/two': 'two']
        '/b.*'      | ''           | ['/b.*': null]
        ~'/b.*'     | ''           | ['/b':['three', 'four'], '/b/0':'three', '/b/1':'four']
        ~'.*2.*'    | 'comp'       | ['/compositeList/2/submapkey1': 'comp map 1 val']
        ''          | 'tw'         | ['/a':['one':'one', 'two': 'two'],'/a/two':'two']
        ''          | '=tw'        | [:]                    // matching paths, but no matching value, return empty map
        ''          | '=two'       | ['/a/two': 'two']
    }

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

    def "jsonObject flattenWithObjects "() {
        given:
        JsonObject jsonObject = new JsonObject(srcMap)

        when:
        Map<String, Object> flatties = jsonObject.flattenWithObjects()

        then:

        flatties.keySet().toList() == ['/a', '/a/one', '/a/two', '/b', '/b/0', '/b/1', '/compositeList', '/compositeList/0', '/compositeList/1', '/compositeList/2/submapkey1']
    }

    def "jsonObject flattenWithObjects 2"() {
        given:
        Map map = [a: [one: 1, twoMap: [two: 2, three: 3]]]
        JsonObject jsonObject = new JsonObject(map)

        when:
        Map<String, Object> flatties = jsonObject.flattenWithObjects()

        then:
        flatties.keySet().toList() == ["/a", "/a/one", "/a/twoMap", "/a/twoMap/two", "/a/twoMap/three"]
    }


        def "should find parent items"() {
            given:
            Map m = [a: [b: [c: [d: 1, d2: 2]]]]
            def flatties = JsonObject.flattenWithLeafObject(m)
            String childPath = '/a/b/c/d'
            String parentPath = JsonObject.getParentPath(childPath)

            when:
            def parentItem = JsonObject.getObjectNodeValue(m, parentPath)
//        def parentItem = JsonObject.getParentItem(childPath, flatties)

            then:
            parentItem instanceof Map
            parentItem.d == 1

        }

        def "should find parent items with regex capture group"() {
            given:
            Map m = [a: [b: [c: [d: 1, d2: 2]]]]
            def flatties = JsonObject.flattenWithLeafObject(m)
            String childPath = '/a/b/c/d'
            String parentPath = JsonObject.getParentPath(childPath)

            when:
            def parentItem = JsonObject.getObjectNodeValue(m, parentPath)
//        def parentItem = JsonObject.getParentItem(childPath, flatties)

            then:
            parentItem instanceof Map
            parentItem.d == 1
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

        def "should remove items by path"() {
            Map m = [a: [b: [c: [d1: 'delete me', d2: 2]]]]
            def flatties = JsonObject.flattenWithLeafObject(m)
            String childPath = '/a/b/c/d1'
            def itemToRemove = JsonObject.getObjectNodeValue(m, childPath)
            String parentPath = JsonObject.getParentPath(childPath)
            Map parentItem = JsonObject.getObjectNodeValue(m, parentPath)

            when:
            assert parentItem.keySet().toList() == ['d1', 'd2']
            def removedItem = JsonObject.removeItem(childPath, m)
//        def parentItem = JsonObject.getParentItem(childPath, flatties)

            then:
            removedItem[childPath] == 'delete me'
            parentItem.keySet().toList() == ['d2']
            m.a.b.c.d2 == 2
            m.a.b.c.d1 == null

        }

        def "should remove parent items by path"() {
            Map m = [a:
                             [b:
                                      [c:
                                               [d1: 'delete me', d2: 2]]]]
            def flatties = JsonObject.flattenWithLeafObject(m)
            String childPath = '/a/b/c'
            def itemToRemove = JsonObject.getObjectNodeValue(m, childPath)
            String parentPath = JsonObject.getParentPath(childPath)
            Map parentItem = JsonObject.getObjectNodeValue(m, parentPath)

            when:
            def removedItem = JsonObject.removeItem(childPath, m)

            then:
            removedItem[childPath] instanceof Map
            removedItem[childPath].keySet().toList() == ['d1', 'd2']
            m.a.b == [:]
            m.a.b.c == null
        }

        def "should remove parent map item by path"() {
            Map m = [
                    a: [
                            b1: [b1sub1: 'delete me', b1sub2: 2],
                            b2: [b2sub1: 'some value', b2sub2: 2],
                    ],
            ]
            def flatties = JsonObject.flattenWithLeafObject(m)
            String childPath = '/a/b1'
            def itemToRemove = JsonObject.getObjectNodeValue(m, childPath)
            String parentPath = JsonObject.getParentPath(childPath)
            Map parentItem = JsonObject.getObjectNodeValue(m, parentPath)

            when:
            assert parentItem.keySet().toList() == ['b1', 'b2']
            def removedResults = JsonObject.removeItem(childPath, m)
//        def parentItem = JsonObject.getParentItem(childPath, flatties)

            then:
            removedResults.keySet().toList() == ['/a/b1']
            removedResults.get(childPath).get('b1sub1') == 'delete me'
            parentItem.keySet().toList() == ['b2']
        }

        def "should remove parent list item by path"() {
            Map m = [
                    mapA : [a: 1, b: 2],
                    listB: [[c: 3, d: 4],
                            [e: 5, f: 6]]]

            String childPath = '/listB/0'
            int origListBSize = m.listB.size()

            when:
            def removedResults = JsonObject.removeItem(childPath, m)

            then:
            origListBSize == 2
            removedResults.keySet().toList() == [childPath]
            removedResults.get(childPath) instanceof Map
            m.listB.size() == 1
            m.listB.size() < origListBSize
        }

        def "flatten a map and return path PlusObject"() {
            when:
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

        def "parse and flatten a json String and return path PlusObject"() {
            when:
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

        def "parse and flatten a json Collection-fragment and return path PlusObject"() {
            when:
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


