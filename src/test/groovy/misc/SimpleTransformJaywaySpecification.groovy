package misc

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.json.JsonSlurper
import spock.lang.Specification

class SimpleTransformJaywaySpecification extends Specification {
    JsonSlurper slurper = new JsonSlurper()
    Map srcMap =  slurper.parseText(src)
    Map destMap =  slurper.parseText(dest)
    DocumentContext srcContext = JsonPath.parse(srcMap)
    DocumentContext destContext = JsonPath.parse(destMap)
    Map rules = slurper.parseText(configsJsonPath)


    def "check jayway read basics"() {
        given:
        String srcPath = '$.properties.collection'

        when:
        def collection = JsonPath.read(srcMap, srcPath)

        then:
        collection == 'MyCollection'
    }


    def "check jayway write basics"() {
        given:
        String srcPath = '$.properties.collection'
        String destPath = '$.properties.collection'

        when:
        def collection = JsonPath.read(srcMap, srcPath)
        def previousDestValue = destContext.read(destPath)
        destContext.set(destPath, collection)
        def updatedDestValue = destContext.read(destPath)

        then:
        previousDestValue == ''
        updatedDestValue == 'MyCollection'
    }


    def "transform leaf nodes with variables"(){
        given:
        String idPath = '$.id'


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
  "coreProperties": {}
}
'''

    public static String dest = '''
 {
   "id" : "#replace#",
   "diagnosticLogging" : true,
   "parserId" : "#replace#",
   "created" : "",
   "coreProperties" : { },
   "description" : "S3 version of FS migration",
   "modified" : "",
   "type" : "#replace#",
   "properties" : {
     "proxyConfig" : {
       "proxyEndpoint" : ""
     },
     "application" : {
       "bucketName" : "f5-data-sample-bucket",
       "objectKeys" : [ "s3-sample-key/" ],
       "region" : "us-west-1"
     },
     "authenticationConfig" : {
       "awsBasicAuthConfig" : {
         "secretKey" : "xXx-Redacted-xXx",
         "accessKey" : "AKIAZ5D5ABDI4BBFV34Q"
       }
     },
     "collection" : ""
   },
   "pipeline" : "",
   "connector" : "lucidworks.s3"
 }
'''

    public static String configsJsonPath = '''
{
    "transformerClass": "FileSystemS3",
    "set": {
        "$.type": "lucidworks.ldap",
        "$.connector": "lucidworks.ldap",
        "$.created": "${new Date()}",
        "$.modified": "${new Date()}",
        "$.properties.security":"$[testmap:true]"
    },
    "copy": {
        "$.id": "$.id",
        "$.pipeline": "$.pipeline",
        "$.parserId": "$.parserId",
        "$.properties.searchProperties.userSearchProp.userFilter": "$.properties.f.ldap_user_filter",
        "$.properties$.searchProperties$.groupSearchProp$.userFilter": "$.properties.f.ldap_group_filter"
    }
}
'''


}
