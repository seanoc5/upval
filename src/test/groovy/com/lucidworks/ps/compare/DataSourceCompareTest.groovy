package com.lucidworks.ps.compare


import groovy.json.JsonSlurper
import spock.lang.Specification

import java.util.regex.Pattern

/**
 * Less of a test suite, more of seeing how things can work...
 */
class DataSourceCompareTest extends Specification {



    def "compare similar F5 s3 datasources with BaseComparator"() {
        given:
        String label = "compare similar F5 s3 datasources with BaseComparator"
        JsonSlurper jsonSlurper = new JsonSlurper()
        def lfile = getClass().getResourceAsStream('/F5.s3.example.json')
        Map left = jsonSlurper.parse(lfile)

        def rfile = getClass().getResourceAsStream('/F5.s3.example2.json')
        Map right = jsonSlurper.parse(rfile)

        when:
        BaseComparator comparator = new BaseComparator(label, left, right)
        CompareObjectResults results = comparator.compare()

        then:
        comparator.leftOnlyKeys.size()==0
        comparator.rightOnlyKeys.size()==0

        results.differences.size()==2
        results.differences[0].differenceType==ComparisonResult.DIFF_VALUES
        results.differences[0].description=='Values are DIFFERENT: left:() and right:(proxy-dmz.intel.com:912)'
        results.differences[1].differenceType==ComparisonResult.DIFF_VALUES
        results.differences[1].description=='Values are DIFFERENT: left:(us-west-1) and right:(us-west-2)'

    }

    def "compare similar F5 s3 datasources with BaseComparator and ignoring proxy value differences"() {
        given:
        String label = "F5 S3 datasource"
        JsonSlurper jsonSlurper = new JsonSlurper()
        def lfile = getClass().getResourceAsStream('/F5.s3.example.json')
        Map left = jsonSlurper.parse(lfile)

        def rfile = getClass().getResourceAsStream('/F5.s3.example2.json')
        Map right = jsonSlurper.parse(rfile)
        Pattern ignoreValues = ~/.*(proxyEndpoint|region).*/

        when:
        BaseComparator comparator = new BaseComparator(label, left, right, )
        CompareObjectResults results = comparator.compare()

        then:
        comparator.leftOnlyKeys.size()==0
        comparator.rightOnlyKeys.size()==0

        results.differences.size()==2
        results.differences[0].differenceType==ComparisonResult.DIFF_VALUES
        results.differences[0].description=='Values are DIFFERENT: left:() and right:(proxy-dmz.intel.com:912)'
        results.differences[1].differenceType==ComparisonResult.DIFF_VALUES
        results.differences[1].description=='Values are DIFFERENT: left:(us-west-1) and right:(us-west-2)'

    }

/*
    def "compare similar F5 s3 datasources with outdated FusionObjectComparator"() {
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
*/

}
