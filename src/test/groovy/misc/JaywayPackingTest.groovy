package misc

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * tests to work through necessary transformations for packaging
 * These include:
 * get a leaf node value
 *  - and then set it to another value
 *  - replace part of the (String) value with something else
 *      - replacement might be result of a method call/evaluation
 * get path elements
 *  - rename them
 *
 */


class JaywayPackingTest extends Specification {
    JsonSlurper slurper = new JsonSlurper()
//    URL idxpUrl = getClass().getResource('/pipelines/Components_TYPEAHEAD_DW_QPL_v4.json')
    URL idxpUrl = getClass().getResource('/components/typeahead/Components_TYPEAHEAD_DW_QPL_v4.json')
    Map idxpMap =  slurper.parse(idxpUrl)
    DocumentContext jaywayContext = JsonPath.parse(idxpMap)


    def "change values manually"() {
        given:
        String srcPath = '$.id'
        String srcPathWithCriteria = '$..[?(@id =~ /Components_'        //$..book[?(@.author =~ /.*REES/i)]

        when:
        def idxpId = JsonPath.read(idxpMap, srcPath)

        then:
        idxpId instanceof String
        idxpId == 'Components_TYPEAHEAD_DW_QPL_v4'
    }


    def "check jayway write basics"() {
        given:
        String srcPath = '$.properties.collection'
        String destPath = '$.properties.collection'

        when:
        def collection = JsonPath.read(idxpMap, srcPath)
        def previousDestValue = destContext.read(destPath)
        destContext.set(destPath, collection)
        def updatedDestValue = destContext.read(destPath)

        then:
        previousDestValue == ''
        updatedDestValue == 'MyCollection'
    }


 /*   def "transform leaf nodes with variables"(){
        given:
        String idPath = '$.id'


    }*/


}
