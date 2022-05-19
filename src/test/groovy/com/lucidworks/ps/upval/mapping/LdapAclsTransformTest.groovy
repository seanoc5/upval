package com.lucidworks.ps.upval.mapping

import groovy.json.JsonSlurper
import spock.lang.Specification

class LdapAclsTransformTest extends Specification {
    Map srcMap = null
    Map destMap = null
    Map rules = null

    /**
     * use the same setup for all the tests (is this an antipattern??)
     *
     * @return
     */
    def setup() {
        JsonSlurper slurper = new JsonSlurper()
        def f4LdapAclsFile = getClass().getClassLoader().getResourceAsStream("examples/F4.ldap-acls.example.json");
        def f5LdapAclsFile = getClass().getClassLoader().getResourceAsStream("templates/F5.ldap-acls.json");
        def rulesfile = getClass().getClassLoader().getResourceAsStream("mapping/F4-F5.ldap-acls.json");
        srcMap = slurper.parse(f4LdapAclsFile)
        destMap = slurper.parse(f5LdapAclsFile)
        rules = slurper.parse(rulesfile)
    }

    def "Transform Set Values"() {
        given:
        ObjectTransformer transformer = new ObjectTransformer(srcMap, destMap, rules)

        when:
        transformer.transformSetValues()

        then:
//        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        transformer.getValueByJsonPath('$.type', destMap) == 'lucidworks.ldap'
        transformer.getValueByJsonPath('$.connector', destMap) == 'lucidworks.ldap'
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

}
