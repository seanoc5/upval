package com.lucidworks.ps.upval.mapping

import groovy.json.JsonSlurper
import spock.lang.Specification

class SimpleTransformSpecification extends Specification {
    Map srcMap = null
    Map destMap = null
    Map rules = null

    /**
     * use the same setup for all the tests (is this an antipattern??)
     *
     * @return
     */
    def setup(){
        JsonSlurper slurper = new JsonSlurper()
        srcMap = slurper.parseText(src)
        destMap = slurper.parseText(dest)
        rules = slurper.parseText(configsJsonPath)
    }

    def "Normal Foo transform calling method directly"() {
        given:
        def transform = new SimpleTransform(srcMap, destMap, rules)

        when:
        String result = transform.testDescription('myBar')

        then:
        result.equals("Creating a test description for thing: (myBar)")
    }

    def "Dynamic function call of foo with hard coded param"() {
        given:
        def transform = new SimpleTransform(srcMap, destMap, rules)
        String func = 'testDescription'

        when:
        String result = transform."${func}"('myBar')

        then:
        result.equals("Creating a test description for thing: (myBar)")
    }

    def "Dynamic class and function call with hard coded param"() {
        given:
        def transformerClass = Class.forName("com.lucidworks.ps.upval.mapping.SimpleTransform");
//        def instance = this.class.classLoader.loadClass( 'SimpleTransform', true)?.newInstance()
        String func = 'testDescription'

        when:
        String result = transformerClass."${func}"('myBar')

        then:
        result.equals("Creating a test description for thing: (myBar)")
    }

    def "Dynamic class and function from config"() {
        given:
        def fu = new SimpleTransform(srcMap, destMap, rules)
        JsonSlurper slurper = new JsonSlurper()
        String className = "com.lucidworks.ps.upval.mapping.SimpleTransform"

        def transformerClass = Class.forName(className)
        String func = 'testDescription'

        when:
        String param1 = 'MyParam1TestValue'
        String result = transformerClass."${func}"(param1)

        then:
        result.equals("Creating a test description for thing: (MyParam1TestValue)")
    }

//    def "simple javax-json jsonPointer testing"(){
//        given:
//        Map json = new JsonSlurper().parseText()
//                JsonPointerImpl jsonPointer =
//        JsonPoi
//        JsonPointer jsonPointer = Json.createPointer("/library");
//        JsonString jsonString = (JsonString) jsonPointer.getValue(jsonStructure);
//
//    }



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
     "collection" : "MyCollection"
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
