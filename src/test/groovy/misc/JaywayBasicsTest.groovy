package misc

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.internal.JsonContext
import groovy.json.JsonSlurper
import net.minidev.json.JSONArray
import spock.lang.Specification

class JaywayBasicsTest extends Specification {
    String src = '''
{
  "id": "sample-s3-id",
  "created": "2021-04-21T19:04:31.111Z",
  "modified": "2021-09-18T00:03:54.998Z",
  "connector": "lucidworks.fs",
  "type": "lucidworks.fs",
  "description": "Sample F4 Filesystem based connector to import JSON filesdata",
  "pipeline": "sample-pipeline",
  "parserId": "sample-parser",
  "properties": {
    "includeDirectories": false,
    "collection": "MyCollection",
    "addFileMetadata": false,
    "initialFilePaths": [
      "/tmp/hsdupload/"
    ],
    "oldFusion": {
      "foo": "bar"
    }
  },
  "updates": [
    {
      "userId": "ashumway",
      "timestamp": "2022-02-08T18:36:17.933Z"
    },
    {
      "userId": "admin",
      "timestamp": "2022-02-08T18:36:17.940Z"
    }
  ]
}
'''
    Map srcMap = new JsonSlurper().parseText(src)
    JsonContext jsonContext = JsonPath.parse(src)
    ObjectTransformerJayway transformerJayway = new ObjectTransformerJayway(srcMap)
    String ALL_PATH = '$..*'            // jayway path syntax for "all nodes/paths"


//    def "read all leafnodes"(){
//
//    }


    def "check jayway read basics"() {
        when:
        def id = transformerJayway.read('$.id')
        def propCollection = transformerJayway.read('$.properties.collection')

        then:
        id == 'sample-s3-id'
        propCollection == 'MyCollection'
    }

    def "test all jsonPaths and values"() {
        given:
        JSONArray paths = transformerJayway.allJsonPaths
        String firstPath = paths[0]
        String lastPath = paths[-1]

        when:

        List values = transformerJayway.srcContext.read(ALL_PATH)
        String firstValue = transformerJayway.srcContext.read(firstPath)
        String lastValue = transformerJayway.srcContext.read(lastPath)

        List pathMatchesProperties = transformerJayway.getPathMatches('properties')
        List pathMatchesRegex = transformerJayway.getPathMatches(~/(created|modified)/)

        then:
        paths instanceof JSONArray
        paths.size() == 21
        paths[0] == '$[\'id\']'
        firstPath == '$[\'id\']'
        paths[-1] == lastPath

        values.size() == 21
        firstValue == 'sample-s3-id'
        lastValue.startsWith('2022')

        pathMatchesProperties.size() == 6
        pathMatchesRegex.size() == 2
    }

    def "perform variable substitution on values"() {
        given:
//        JsonContext pathsContext = JsonPath.parse(src)
        Map varSubstitutions = [
                update: [[path: '$.id', from: 'sample', to: 'MyIdHere'],
                         [from: 'sample', to: 'Acme'],
                         [from: ~'/tmp/([^/+])/', to: '/opt/uploads/$1']
                ],
                rename: [
                        [from: 'oldFusion', to: 'newFusion']
                ],
        ]
        varSubstitutions.each { String operation, List<Map<String, Object>> instructions ->
            if (operation == 'update') {
                instructions.each { Map instructionMap ->
                    log.info "$operation) Instruction map: $instructionMap "
                    List matchingPaths = []
                    String path = instructions.path
                    if (path) {
                        matchingPaths = transformerJayway.getPathMatches(path)
                        println "Found paths: $matchingPaths"
                    }
                    String matchValue
//                if(
                    matchingPaths.each { String matchedPath ->
                        String origValue = transformerJayway.read(matchedPath)
                        println "Path: ($matchedPath) -- orig val:($origValue)"
                    }
                }
            }
        }
        when:
        List<String> stringMatches = transformerJayway.getPathsByValue('sample')
        List<String> regexMatches = transformerJayway.getPathsByValue(~/[lL]ucidworks/)

        then:
        stringMatches.size() == 3
        regexMatches.size() == 2
    }

    def "perform variable substitution"() {
        given:
        Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
        JsonContext jsonPathsContext = JsonPath.using(conf).parse(src)
        JsonContext jsonContext = JsonPath.parse(src)
        JSONArray paths = jsonPathsContext.read(ALL_PATH)

        when:
        List<String> stringMatches = ObjectTransformerJayway.getPathsByValue(jsonContext, paths, 'sample')
        List<String> regexMatches = ObjectTransformerJayway.getPathsByValue(jsonContext, paths, ~/[lL]ucidworks/)

        then:
        stringMatches.size() == 3
        regexMatches.size() == 2
    }

    def "jayway read more advanced"() {
        when:
        def updates = pathsContext.read('$..updates')
        def userIds = pathsContext.read('$..updates[*].userId')
        def updateAdmin = pathsContext.read('$..updates[?(@.userId=="admin")]')
        def updateAdminTimeStamps = pathsContext.read('$..updates[?(@.userId=="admin")].timestamp')        // always returns a list?
        def updatePos1 = pathsContext.read('$..updates[1]')
        def fooVal = pathsContext.read('$..*[?(@.*=="foo")]')


        then:
        updates.size() == 1
        userIds.size() == 2
        userIds == ['ashumway', 'admin']
        updateAdmin.userId == ['admin']
        updatePos1.userId == ['admin']
        updateAdminTimeStamps[0].startsWith('2022')
    }


    def "jayway read from json string"() {
        given:
        Map map = new JsonSlurper().parseText(src)
        String srcPath = '$.properties.collection'
        String destPath = '$.properties.collection'

        when:
        def updates = JsonPath.read(map, '$..updates')
        def updateChildren = JsonPath.read(map, '$..updates[*]')
        def props = JsonPath.read(map, '$.properties')

        then:
        updates.size() == 1
        updateChildren.size() == 2
        props.size() == 4
    }

    def "write values with jayway"() {
        given:
//        Map map = new JsonSlurper().parseText(src)
        JsonContext jsonContext = JsonPath.parse(src)
        def srcPath = '$.properties.includeDirectories'

        when:
        def incDir = jsonContext.read(srcPath)
        def jsonObject = jsonContext.json
        assert jsonObject.properties.includeDirectories == false

        jsonContext.set(srcPath, true)
        assert jsonObject.updates.size() == 2
        jsonContext.delete('$.updates')

        then:
        jsonObject.properties.includeDirectories == true
        jsonObject.updates == null

    }


    def "delete entries simple"() {
        given:
        JsonContext jsonContext = JsonPath.parse(src)
        Map objectsMap = jsonContext.json()
        String jayPathUpdates = '$..updates'
        assert objectsMap.updates != null


        when:
        def preDelete = jsonContext.read(jayPathUpdates)
        def deleteUpdatesResult = jsonContext.delete(jayPathUpdates)
        def postDelete = jsonContext.read(jayPathUpdates)

        then:
        preDelete instanceof Collection
        preDelete.size() == 1
        preDelete[0].size() == 2
        objectsMap.updates == null
        postDelete instanceof Collection
        postDelete.size() == 0

    }

    /** cannot do this... need a path object, no existing "all paths...? */
    def "confirm no filtering by value alone"(){
        when:
        String bar = '$..[?(@id =~ /Components_/)]'
        def foo = transformerJayway.read(bar)

        then:
        thrown(com.jayway.jsonpath.InvalidPathException)
        bar.containsIgnoreCase('@')
        foo == null         // path above is invalid, nothing returned for foo
    }


/*
    def "delete entries complex paths"() {
        given:
        JsonContext jsonContext = JsonPath.parse(src)
        Map objectsMap = jsonContext.json()
        String jayPathUpdates = '$..updates'
        assert objectsMap.updates != null


        when:
        def preDelete = jsonContext.read(jayPathUpdates)
        def deleteUpdatesResult = jsonContext.delete(jayPathUpdates)
        def postDelete = jsonContext.read(jayPathUpdates)

        then:
        preDelete instanceof Collection
        preDelete.size() == 1
        preDelete[0].size() == 2
        objectsMap.updates == null
        postDelete instanceof Collection
        postDelete.size() == 0

    }
*/


}
