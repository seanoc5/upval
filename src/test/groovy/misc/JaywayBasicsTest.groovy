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
    ]
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

    JsonContext jsonContext = JsonPath.parse(src)
    String ALL_PATH = '$..*'            // jayway path syntax for "all nodes/paths"


    def "check jayway read basics"() {
        when:
        def id = jsonContext.read('$.id')
        def propCollection = jsonContext.read('$.properties.collection')

        then:
        id == 'sample-s3-id'
        propCollection == 'MyCollection'
    }

    def "test all jsonPaths and values"() {
        given:
        Configuration conf = Configuration.builder().options(Option.AS_PATH_LIST).build();
        JsonContext jsonPathsContext = JsonPath.using(conf).parse(src)


        when:
        JSONArray paths = jsonPathsContext.read(ALL_PATH)
        String firstPath = paths[0]
        String lastPath = paths[-1]

        List values = jsonContext.read(ALL_PATH)
        String firstValue = jsonContext.read(firstPath)
        String lastValue = jsonContext.read(lastPath)

        List pathMatchesProperties = ObjectTransformerJayway.getPathMatches(paths, 'properties')
        List pathMatchesRegex = ObjectTransformerJayway.getPathMatches(paths, ~/(created|modified)/)

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
        pathMatchesRegex.size()==2
    }

    def "perform variable substitution on values"() {
        given:
        JsonContext jsonContext = JsonPath.parse(src)
        Map varSubstitutions = [
                '$.id': [from:'sample', to:'MyIdHere']
        ]
        varSubstitutions.each { String subsPath, Map subsMap ->
            List matchingPaths = jsonContext.read(subsPath)
            matchingPaths.each {String matchedPath ->
                String origValue =
        }
        when:
        List<String> stringMatches = ObjectTransformerJayway.getPathsByValue(jsonContext, paths, 'sample')
        List<String> regexMatches = ObjectTransformerJayway.getPathsByValue(jsonContext, paths, ~/[lL]ucidworks/)

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
        def updates = jsonContext.read('$..updates')
        def userIds = jsonContext.read('$..updates[*].userId')
        def updateAdmin = jsonContext.read('$..updates[?(@.userId=="admin")]')
        def updateAdminTimeStamps = jsonContext.read('$..updates[?(@.userId=="admin")].timestamp')        // always returns a list?
        def updatePos1 = jsonContext.read('$..updates[1]')
        def fooVal = jsonContext.read('$..*[?(@.*=="foo")]')


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

    // todo -- can't find a way to match "any" element with a given value, value selector seems to need a 'thing' to look in, no ?(@.*...) or similar
    def "jayway transform TA objects"() {
        given:
        JsonSlurper slurper = new JsonSlurper()
        URL url = getClass().getResource('/components/typeahead/ta-objects.json')
        Map objectsMap = slurper.parse(url)
        // upval JsonObject -- for debugging, not needed
//        JsonObject jsonObject = new JsonObject(objectsMap)

        // Jayway context
        JsonContext jsonContext = JsonPath.parse(url)
//        String jpath = '$..*[[?(@.from_user =~ /.*\$.*/i)]'
//        Map transforms = ['$.collections


        when:
        Map objects = objectsMap.objects
        // just some debugging, can remove JsonObject from this Jayway test
//        def varObjects = jsonObject.findItems('', '$')
//        varObjects.each { key, val ->
//            println("key: $key -> $val")
//        }

        then:
        objectsMap.keySet().size() == 3
        objectsMap.objects
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
