package com.lucidworks.ps.upval.mapping

import groovy.json.JsonSlurper
import spock.lang.Specification

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

    def "Transform Set Values"(){
        given:
        ObjectTransformer transformer = new ObjectTransformer(srcMap, destMap, rules)

        when:
        transformer.transformSetValues()

        then:
//        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        transformer.getValueByJsonPath('$.type', destMap) == 'lucidworks.fs'
        transformer.getValueByJsonPath('$.connector', destMap) == 'lucidworks.fs'
        transformer.getValueByJsonPath('$.created', destMap).contains('2022')

    }

    def "Transform"() {
        given:
        ObjectTransformer transformer = new ObjectTransformer(srcMap, destMap, rules)

        when:
        transformer.transform()

        then:
//        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        transformer.getValueByMapPath('/type', destMap) == 'lucidworks.ldap'
    }

    public static String src = '''
{
  "id": "psg_hsd-es-fs",
  "created": "2021-04-21T19:04:31.111Z",
  "modified": "2021-09-18T00:03:54.998Z",
  "connector": "lucidworks.fs",
  "type": "lucidworks.fs",
  "description": "Filesystem based connector to import JSON files for HSD-ES bug data",
  "pipeline": "psg_hsd-es-fs-index",
  "parserId": "psg_json-parser",
  "properties": {
    "includeDirectories": false,
    "collection": "CircuitSearch",
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
