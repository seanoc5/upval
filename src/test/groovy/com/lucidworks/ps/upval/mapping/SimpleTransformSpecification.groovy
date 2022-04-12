package com.lucidworks.ps.upval.mapping

import groovy.json.JsonSlurper
import spock.lang.Specification

class SimpleTransformSpecification extends Specification {
    def "Normal Foo transform calling method directly"() {
        given:
        def transform = new SimpleTransform()

        when:
        String result = transform.foo('myBar')

        then:
        result.equals("In foo(myBar)")
    }

    def "Dynamic function call of foo with hard coded param"() {
        given:
        def transform = new SimpleTransform()
        String func = 'foo'

        when:
        String result = transform."${func}"('myBar')

        then:
        result.equals("In foo(myBar)")
    }

    def "Dynamic class and function call with hard coded param"() {
        given:
//        def transform = new SimpleTransform()
        def foo = Class.forName("com.lucidworks.ps.upval.mapping.SimpleTransform");
//        def instance = this.class.classLoader.loadClass( 'SimpleTransform', true)?.newInstance()
        String func = 'foo'

        when:
        String result = foo."${func}"('myBar')

        then:
        result.equals("In foo(myBar)")
    }

    def "Dynamic class and function from config"() {
        given:
        def fu = new SimpleTransform()
        JsonSlurper slurper = new JsonSlurper()
        Map json =  slurper.parseText(spoMapping4_5)
        String transformerClass = json.transformerClass
        String className = "com.lucidworks.ps.upval.mapping.SimpleTransform"

        def foo = Class.forName(className)
        String func = 'foo'

        when:
        String result = foo."${func}"('myBar')

        then:
        result.equals("In foo(myBar)")
    }

    def "simple javax-json jsonPointer testing"(){
        given:
        Map json = new JsonSlurper().parseText(spoMapping4_5)
//                JsonPointerImpl jsonPointer =
//        JsonPoi
//        JsonPointer jsonPointer = Json.createPointer("/library");
//        JsonString jsonString = (JsonString) jsonPointer.getValue(jsonStructure);

    }



    String spoMapping4_5 = '''{
      "transformerClass": "SimpleTransform",
      "set": {
        "type": "lucidworks.sharepoint-optimized",
        "connector": "lucidworks.sharepoint-optimized"
      },
      "copy": {
        "id": "id",
        "pipeline": "pipeline",
        "parserId": "parserId",
        "properties|webApplication|webApplicationUrl": "properties|startLinks",
        "properties|webApplication|inclusiveRegexes": "properties|inclusiveRegexes",
        "properties|webApplication|exclusiveRegexes": "properties|exclusiveRegexes",
        "properties|webApplication|includedFileExtensions": "properties|includeExtensions",
        "diagnosticLogging": "diagnosticMode",
        "ntlmProperties|username": "f.username",
        "ntlmProperties|password": "f.password"
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
