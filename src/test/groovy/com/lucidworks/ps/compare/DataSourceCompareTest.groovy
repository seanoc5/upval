package com.lucidworks.ps.compare


import groovy.json.JsonSlurper
import spock.lang.Specification
/**
 * Less of a test suite, more of seeing how things can work...
 */
class DataSourceCompareTest extends Specification {


    def "compare similar F5 s3 datasources"() {
        given:
        JsonSlurper jsonSlurper = new JsonSlurper()
        def lfile = getClass().getResourceAsStream('/F5.s3.example.json')
        Map left = jsonSlurper.parse(lfile)

        def rfile = getClass().getResourceAsStream('/F5.s3.example2.json')
        Map right = jsonSlurper.parse(rfile)



        when:
        FusionObjectComparator comparator = new FusionObjectComparator('dataSource', left, right)

        then:
        comparator
    }



}
