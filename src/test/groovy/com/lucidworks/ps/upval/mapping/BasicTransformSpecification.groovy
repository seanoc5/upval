package com.lucidworks.ps.upval.mapping

import groovy.json.JsonSlurper
import spock.lang.Specification
//import javax.
class BasicTransformSpecification extends Specification {
    def "Transform foo property"() {
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

    String basicMapping = '''{
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
          "apiQueryRowLimit": 5000,
          "webApplication": {
            "inclusiveRegexes": [],
            "exclusiveRegexes": [],
            "siteCollections": [],
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
