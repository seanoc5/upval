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

//    def "simple javax-json jsonPointer testing"(){
//        given:
//        Map json = new JsonSlurper().parseText()
//                JsonPointerImpl jsonPointer =
//        JsonPoi
//        JsonPointer jsonPointer = Json.createPointer("/library");
//        JsonString jsonString = (JsonString) jsonPointer.getValue(jsonStructure);
//
//    }




}
