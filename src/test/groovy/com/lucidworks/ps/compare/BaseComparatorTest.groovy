package com.lucidworks.ps.compare


import spock.lang.Specification

import java.util.regex.Pattern

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/12/22, Sunday
 * @description:
 */

class BaseComparatorTest extends Specification {
    static final Map LEFT_MAP = [
            top1: 'first top node',             // same
            top2: [1, 2, 3],                    // diff values (4)
            top3: [                             // same
                    middle1: 'top3-middle1',    // diff text
                    middle2: [4, 5, 6],         // same
                    middle3: [                  // different: missing bottom2
                            bottom1: 'top3-middle3-bottom1'     // same
                    ]]]
    static final Map RIGHT_MAP = [
            top1: 'first top node',
            top2: [1, 2, 3, 4],
            top3: [
                    middle1: 'top3-middle1 plus change',
                    middle2: [4, 5, 6],
                    middle3: [
                            bottom1: 'top3-middle3-bottom1',
                            bottom2: 'new bottom element']]]


    def "Compare simple maps"() {
        given:
        String label = 'Compare unit test'
        BaseComparator comparator = new BaseComparator(label, LEFT_MAP, RIGHT_MAP,)

        when:
        CompareObjectResults results = comparator.compare('GenericObjectMap')

        then:
        results.leftOnlyKeys == null

        results.rightOnlyKeys.size() == 2

        results.sharedKeys.size() == 9

        results.differences.size() == 2
        results.differences[0].differenceType == ComparisonResult.DIFF_RIGHT_ONLY
        results.differences[0].description == '[top2.[3], top3.middle3.bottom2]'
        results.differences[1].differenceType == ComparisonResult.DIFF_VALUES
        results.differences[1].description == 'Values are DIFFERENT: left:(top3-middle1) and right:(top3-middle1 plus change)'
    }

    def "Compare ignoring values for top3.middle2"() {
        given:
        String label = 'Compare unit test'
        Pattern ignoreValues = ~/(top2.*|top3.middle1)/
        BaseComparator comparator = new BaseComparator(label, LEFT_MAP, RIGHT_MAP, ignoreValues)

        when:
        CompareObjectResults results = comparator.compare()
        def similarButDifferent = results.similarities.findAll{it.differenceType==ComparisonResult.SIMILAR}

        then:
        results.leftOnlyKeys == null

        results.rightOnlyKeys.size() == 2

        results.sharedKeys.size() == 9

        results.differences.size() == 1
        results.differences[0].differenceType == ComparisonResult.DIFF_RIGHT_ONLY
        results.differences[0].description == '[top2.[3], top3.middle3.bottom2]'

        results.similarities.size() == 9
        similarButDifferent.size()==2

    }
    def "Compare comparing values for top*"() {
        given:
        String label = 'Compare unit test'
        Pattern ignoreValues = ~/top3.middle1/
        BaseComparator comparator = new BaseComparator(label, LEFT_MAP, RIGHT_MAP, ignoreValues)

        when:
        CompareObjectResults results = comparator.compare()
        def similarButDifferent = results.similarities.findAll{it.differenceType==ComparisonResult.SIMILAR}

        then:
        results.leftOnlyKeys == null

        results.rightOnlyKeys.size() == 2

        results.sharedKeys.size() == 9

        results.differences.size() == 1
        results.differences[0].differenceType == ComparisonResult.DIFF_RIGHT_ONLY
        results.differences[0].description == '[top2.[3], top3.middle3.bottom2]'

        results.similarities.size() == 9
        similarButDifferent.size()==2

    }

}
