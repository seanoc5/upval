package misc


import groovy.json.JsonSlurper
import spock.lang.Specification

class ObjectTransformerJaywayTest extends Specification {
    JsonSlurper slurper = new JsonSlurper()

    Map srcMap = slurper.parseText(src)
    Map destMap = slurper.parseText(dest)           // todo -- remove destMap, revert back to just
//    Map rules = [copy:

    Map rules = slurper.parseText(configsJsonPathDollarDotFormat)


    def "simple find occurences of values"() {
        given:
        List<String> answertKeys = [
                        '$[\'id\']',
                        '$[\'stages\'][0][\'condition\']',
                        '$[\'stages\'][1][\'condition\']',
                        '$[\'properties\'][\'secretSourcePipelineId\']'
                ]
        JsonSlurper slurper = new JsonSlurper()
        URL idxpUrl = getClass().getResource('/components/typeahead/Components_TYPEAHEAD_DW_QPL_v4.json')
        Map idxpMap = slurper.parse(idxpUrl)
//        DocumentContext jaywayContext = JsonPath.parse(idxpMap)

        ObjectTransformerJayway transformer = new ObjectTransformerJayway(idxpMap)

        when:
        List<String> paths = transformer.getPathsByValue('Components_')
        Map resultMap = transformer.read(paths)

        then:
        resultMap.size() == answertKeys.size()
        resultMap.keySet().containsAll(answertKeys)
        resultMap.get('$[\'id\']') == 'Components_TYPEAHEAD_DW_QPL_v4'


    }

    def "simple update sourceMap values"() {
        given:
        Map idxpMap = slurper.parse(getClass().getResource('/components/typeahead/Components_TYPEAHEAD_DW_QPL_v4.json'))
        List<Map<String, Object>> updateRules = [
                [from: 'Components_', to: '${AppName}'],
                [from: ~'Components_', to: '${AppName}'],
        ]
        ObjectTransformerJayway transformer = new ObjectTransformerJayway(idxpMap)

        when:
        List<String> paths = transformer.getPathsByValue('Components_')
        Map resultMap = transformer.read(paths)

        then:
        resultMap.size() > 1

    }

    def "Transformer should transform src with rules and dest template via static call"() {
        when:
//        def result = ObjectTransformerJayway.transform(srcMap, rules, destMap)
        def result = ObjectTransformerJayway.transform(srcMap, rules)

        then:
        result.size() == 0      // todo replace me
        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        transformer.getValueByJsonPath('$.type', destMap) == 'lucidworks.ldap'
        transformer.getValueByJsonPath('$.connector', destMap) == 'lucidworks.ldap'
        transformer.getValueByJsonPath('$.created', destMap).contains('2022')
    }

    def "should transform with static calls"() {
        when:
        def jaywayTransformer = ObjectTransformerJayway.transform(srcMap, rules, destMap)

        then:
        jaywayTransformer.getValueByMapPath('/type', destMap) == 'lucidworks.ldap'
    }

/**
 * @deprecated ignore me--testing passing around enclosures
 * @return
 */
    def "lamda experiment"() {
        given:
        def lambdaNorma = { Object it -> return it.class.name }
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

    public static String configsJsonPathDollarDotFormat = '''
{
    "transformerClass": "SimlpeTransform",
    "set": {
        "$.type": "lucidworks.ldap",
        "$.connector": "lucidworks.ldap",
        "$.created": "${now}",
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
