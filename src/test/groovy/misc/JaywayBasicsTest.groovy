package misc

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.internal.JsonContext
import com.lucidworks.ps.transform.JsonObject
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

class JaywayBasicsTest extends Specification {


    def "check jayway read basics"() {
        given:
        Map map = [leafa  : 'myleaf',
                   listb  : [9, 8, 7, 'bar'],
                   submapc: [foo: 'bar']]

        when:
        def leafa = JsonPath.read(map, '$.leafa')
        def listb = JsonPath.read(map, '$.listb')


        then:
        leafa == 'myleaf'
        listb == [9, 8, 7, 'bar']
    }


    def "jayway read more advanced"() {
        given:
        Map map = [leafa  : 'myleaf',
                   listb  : [
                           [id: 1, tag: 'a'],
                           [id: 2, tag: 'b'],
                           [id: 3, tag: 'c'],
                   ],
                   updates: [
                           ["userId": "admin", "timestamp": "2021-04-14T06:28:02.447Z"],
                           ["userId": "ashumway", "timestamp": "2021-12-16T19:22:31.713Z"],
                   ],
                   map2   : [submap3: [1, 2, 3], subleaf4: 'foo']
        ]

        when:
        def updates = JsonPath.read(map, '$..updates')
        def userIds = JsonPath.read(map, '$..updates[*].userId')
        def updateAdmin = JsonPath.read(map, '$..updates[?(@.userId=="admin")]')
        def updateAdminTimeStamps = JsonPath.read(map, '$..updates[?(@.userId=="admin")].timestamp')        // always returns a list?
        def updatePos1 = JsonPath.read(map, '$..updates[1]')
        def fooVal = JsonPath.read(map, '$..*[?(@.*=="foo")]')


        then:
        updates.size() == 1
        userIds.size() == 2
        userIds == ['admin', 'ashumway']
        updateAdmin.userId == ['admin']
        updatePos1.userId == ['ashumway']
        updateAdminTimeStamps[0].startsWith('2021')
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

    /*public static*/
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

}
