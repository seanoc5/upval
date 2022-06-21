package com.lucidworks.ps.upval.transform

import com.lucidworks.ps.transform.ObjectTransformerJayway
import groovy.json.JsonSlurper
import spock.lang.Specification

class ObjectTransformerTest extends Specification {
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

/*
    def "should transform a source to expected output" () {
        given
        ObjectTransformerJayway transformer = new ObjectTransformerJayway(srcMap, destMap, rules)

        when:

    }
*/

    def "Transform Set Values"(){
        /*
        "/type": "lucidworks.ldap",
         "/connector": "lucidworks.ldap",
         "/created": "${new Date()}",
         "/modified": "${new Date()}",
         */
        given:
        ObjectTransformerJayway transformer = new ObjectTransformerJayway(srcMap, destMap, rules)

        when:
        transformer.setValues()

        then:
//        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        transformer.getValueByJsonPath('$.type', destMap) == 'lucidworks.ldap'
        transformer.getValueByJsonPath('$.connector', destMap) == 'lucidworks.ldap'
        transformer.getValueByJsonPath('$.created', destMap).contains('2022')

    }

    def "Transform"() {
        given:
        ObjectTransformerJayway transformer = new ObjectTransformerJayway(srcMap, destMap, rules)

        when:
        transformer.transform()

        then:
//        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        transformer.getValueByMapPath('/type', destMap) == 'lucidworks.ldap'
    }

    def "lamda experiment"(){
        given:
        def lambdaNorma = {Object it -> return it.class.name}
        String myLambdaStr = '{Object it -> return "foo:...${it}"}'

        when:
        def simpleEval = Eval.me("2+2")
        def lambdaEval = Eval.me(myLambdaStr)
        def resultEval = lambdaEval('Foo')

        then:
        simpleEval == 4
        lambdaEval.class.name == 'Script1$_run_closure1'
        resultEval == 'foo:...Foo'
    }

//    def "transform with function from config json"(){
//        given:
//        Map myMap = [a:[one:'1a', two:'2b']]
//
//        when:
//        def myEntry =
//    }

/*
    def "GetByMapPath"() {
    }

    def "SetByMapPath"() {
    }*/

    public static String src = '''
{
    "id" : "my_abc_acl",
    "created" : "2020-04-17T06:20:05.291Z",
    "modified" : "2020-04-17T06:20:05.291Z",
    "connector" : "lucid.ldap-acls",
    "type" : "ldap-acls",
    "pipeline" : "_system",
    "properties" : {
        "refreshOlderThan" : -1,
        "startLinks" : [ "ldap://my.company.com:389" ],
    }
}    
'''

    public static String dest = '''
{
   
    "id": "replaceme-destination",
    "type": "overrideme.ldap",
    "properties": {
        "security": {},
        "searchProperties": {
            "apiQueryRowLimit": 1000,
            "userSearchProp": {
                "crawlForUsers": true,
                "userFilter": "(&(objectclass=user)(sAMAccountName=*))"
            },
            "followReferrals": false,
            "groupSearchProp": {
                "groupFilter": "(&(objectclass=group))",
                "crawlForGroups": true
            },
            "useGlobalCatalog": false
        },
        "collection": "${dataSource.id}-access-control-hierarchy"
    },
    "connector": "lucidworks.ldap"
}
'''

    public static String configsJsonPath = '''
{
    "transformerClass": "SimlpeTransform",
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
