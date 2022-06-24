package com.lucidworks.ps.transform


import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * outdated transform test using older jsonpath format
 * @deprecated
 * todo change to use current path with slashes...?
 */
class FStoS3ObjectTransformerTest extends Specification {
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

    def "should Transform Set Values from explicit map"(){
        given:
        ObjectTransformerJayway transformer = new ObjectTransformerJayway(srcMap, destMap, rules)

        when:
        transformer.setValues()

        then:
//        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        transformer.getValueByJsonPath('$.type', destMap) == 'lucidworks.fs'
        transformer.getValueByJsonPath('$.connector', destMap) == 'lucidworks.fs'
        transformer.getValueByJsonPath('$.created', destMap).contains('2022')

    }

    def "Transform"() {
        given:
        ObjectTransformerJayway transformer = new ObjectTransformerJayway()

        when:
        def foo = ObjectTransformerJayway.transform(srcMap, destMap, rules)

        then:
//        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        transformer.getValueByMapPath('/type', destMap) == 'lucidworks.ldap'
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
    "remove": {
    
}
'''


    public static String configsSlashy = '''
{
    "transformerClass": "SimlpeTransform",
    "set": {
        "/type": "lucidworks.ldap",
        "/connector": "lucidworks.ldap",
        "/created": "${new Date()}",
        "/modified": "${new Date()}",
    },
    "copy": {
        "/id": "/id",
        "/pipeline": "/pipeline",
        "/parserId": "/parserId",
        "/properties/searchProperties/userSearchProp/userFilter": "/properties/f.ldap_user_filter",
        "/properties/searchProperties/groupSearchProp/userFilter": "/properties/f.ldap_group_filter"
    }
}
'''
}
