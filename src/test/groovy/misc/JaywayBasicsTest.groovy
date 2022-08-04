package misc

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.internal.JsonContext
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


    def "check jayway read more advanced"() {
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
        def updatePos1 = JsonPath.read(map, '$..updates[1]')
        def fooVal = JsonPath.read(map, '$..*[?(@.*=="foo")]')


        then:
        updates.size() == 1
        userIds.size() == 2
        userIds == ['admin', 'ashumway']
        updateAdmin.userId == ['admin']
        updatePos1.userId == ['ashumway']
    }


    def "check jayway read from json string"() {
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

        jsonContext.set(srcPath, true)
        assert jsonObject.updates.size() == 2
        jsonContext.delete('$.updates')

        then:
        jsonObject.properties.includeDirectories == true
        jsonObject.updates == null

    }


    public static String src = '''
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
      "userId": "ashumway",
      "timestamp": "2022-02-08T18:36:17.940Z"
    }
  ]
}
'''

}
