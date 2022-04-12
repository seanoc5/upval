package com.lucidworks.ps.upval.mapping

import groovy.json.JsonSlurper
import spock.lang.Specification

class BasicTransformSpecification extends Specification {
    def "Translate foo source path to destination"() {
        given:
        JsonSlurper slurper = new JsonSlurper()
        def srcMap = slurper.parseText(srcConfig)
        def destMap = slurper.parseText(destTemplate)
        def mappingRules = 1

        when:
        String result = transform.foo('myBar')

        then:
        result.equals("In foo(myBar)")
    }

/*
    def "foo"() {
        given:

        when:

        then:
    }
*/

    String srcConfig = '''
{
      "id" : "my_abc_acl",
      "created" : "2020-04-17T06:20:05.291Z",
      "modified" : "2020-04-17T06:20:05.291Z",
      "connector" : "lucid.ldap-acls",
      "type" : "ldap-acls",
      "pipeline" : "_system",
      "properties" : {
        "refreshOlderThan" : -1,
        "f.do_not_follow_referrals" : false
      }
}      
'''

    String destTemplate = '''
{
    "id" : "",
    "properties" : {
        "collection" : "acls"
    }
}   
'''
//    def

    String transformInstructions = '''{
      "transformerClass": "SimpleTransform",
      "set": {
        "/type": "lucidworks.ldap-acls",
        "/connector": "lucidworks.ldap-acls"
      },
      "copy": {
        "/id": "id",
        "/pipeline": "/pipeline",
        "/parserId": "/parserId",
        "/diagnosticLogging": "/diagnosticMode",
        "/properties/startLinks" : 
      },
      "remove": {
        "builtInFieldNames": [
          "_no_longer_used_"
        ]
      },
      "defaults": {
        "properties": {
          "siteCollectionDeletionThreshold": 336,
          "apiQueryRowLimit": 5000,
          "changeApiQueryRowLimit": 2000,
          "webApplication": {
            "inclusiveRegexes": [],
            "doNotRunExporter": false,
            "excludeContentsExtensions": [],
            "exclusiveRegexes": [],
            "excludedFileExtensions": [],
            "siteCollections": [],
            "includeContentsExtensions": [],
            "forceFullCrawl": false,
            "restrictToSpecificSubsites": [],
            "fetchSiteCollections": true,
            "restrictToSpecificItems": [],
            "regexCacheSize": 10000,
            "includedFileExtensions": []
          },
          "aclCommitAfter": 60000,
          "solrSocketTimeout": 60000,
          "solrConnectionTimeout": 60000,
          "contentCommitAfter": 60000,
          "collection": "Search"
        }
      }
    }'''

}
