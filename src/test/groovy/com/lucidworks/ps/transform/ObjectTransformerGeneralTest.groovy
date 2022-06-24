package com.lucidworks.ps.transform


import spock.lang.Specification

class ObjectTransformerGeneralTest extends Specification {


    def "should get elements with gpath"() {
        given:
        Map srcMap = [a: [one: 1, two: 2], b: ['first', 'second']]

        when:
        def aOne = srcMap.a.one
        def b0 = srcMap.b[0]

        then:
        aOne == 1
        b0 == first
    }

    def "should get elements with "() {
        given:
        Map srcMap = [a: [one: 1, two: 2], b: ['first', 'second']]
        Map destMap = [a: [one: 0, two: 0], b: ['first', 'second', 'third']]
        String newLeafValue = 'new leaf value here'
        Map rules = [
                copy: ['.*'],
                set : ['/c/newLeaf1': newLeafValue],
                remove: ['/b/1']
        ]

        when:
//        transformer.transform(srcMap, destMap, rules)
        def foo = ObjectTransformerJayway.transform(srcMap, destMap, rules)


        then:
//        transformer.getByMapPath('/id', destMap) == 'my_abc_acl'
        foo.size() == -1        // todo fix refactoring... new checks
        foo.getValueByMapPath('/type', destMap) == 'lucidworks.ldap'
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


}
