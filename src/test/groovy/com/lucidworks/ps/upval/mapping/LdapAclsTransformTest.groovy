package com.lucidworks.ps.upval.mapping

import com.lucidworks.ps.mapping.ObjectTransformerJayway
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
        def rulesfile = getClass().getClassLoader().getResourceAsStream("mapping/F4-F5.ldap-acls.jayway.json");
        srcMap = slurper.parse(f4LdapAclsFile)
        destMap = slurper.parse(f5LdapAclsFile)
        rules = slurper.parse(rulesfile)
    }

    def "Transform Set Values"() {
        given:
        ObjectTransformerJayway transformer = new ObjectTransformerJayway(srcMap, destMap, rules)

        when:
        def foo = transformer.transform()

        then:
//        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        transformer.destContext.read('$.type') == 'lucidworks.ldap'
        transformer.destContext.read('$.connector') == 'lucidworks.ldap'
        transformer.destContext.read('$.created').contains('2022')

    }


/*
    def "Transform"() {
        given:
        ObjectTransformerJayway transformer = new ObjectTransformerJayway(srcMap, destMap, rules)

        when:
        transformer.transform()

        then:
//        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        transformer.getValueByMapPath('/type', destMap) == 'lucidworks.ldap'
    }*/

}
