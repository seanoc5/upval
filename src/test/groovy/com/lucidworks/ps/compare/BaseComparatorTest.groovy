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
                    ],
            ],
            created:'20220710'                  // simulate a datestamp difference (like we will want to ignore
    ]
    static final Map RIGHT_MAP = [
            top1: 'first top node',
            top2: [1, 2, 3, 4],
            top3: [
                    middle1: 'top3-middle1 plus change',
                    middle2: [4, 5, 6],
                    middle3: [
                            bottom1: 'top3-middle3-bottom1',
                            bottom2: 'new bottom element'],
            ],
            created:'20220711'

    ]


    def "Compare simple maps"() {
        given:
        String label = 'Compare unit test'
        BaseComparator comparator = new BaseComparator(label, LEFT_MAP, RIGHT_MAP)

        when:
        CompareJsonObjectResults results = comparator.compare()

        then:
        results.leftOnlyKeys == null

        results.rightOnlyKeys.size() == 2

        results.sharedKeys.size() ==10

        results.differences.size() == 3
        results.differences[0].differenceType == ComparisonResult.DIFF_RIGHT_ONLY
        results.differences[0].description == '[/top2/3, /top3/middle3/bottom2]'    // '[top2.[3], top3.middle3.bottom2]'
        results.differences[1].differenceType == ComparisonResult.DIFF_VALUES
        results.differences[1].description == 'Values are DIFFERENT: left:(top3-middle1) and right:(top3-middle1 plus change)'
    }

    def "compare simple maps while ignoring value differences /top2.* and /top3/middle1"() {
        given:
        String label = 'Compare unit test'
        Pattern ignoreValues = ~/(\/top2.*|\/top3\/middle1)/
        BaseComparator comparator = new BaseComparator(label, LEFT_MAP, RIGHT_MAP, ignoreValues)

        when:
        CompareJsonObjectResults results = comparator.compare()
        def differences = results.differences
        def similarities = results.similarities
        def similarButDifferent = results.similarities.findAll{it.differenceType==ComparisonResult.SIMILAR}

        then:
        results.leftOnlyKeys == null
        results.rightOnlyKeys.size() == 2
        results.sharedKeys.size() == 10

        differences.size() == 2
        differences[0].differenceType == ComparisonResult.DIFF_RIGHT_ONLY
        differences[0].description ==  '[/top2/3, /top3/middle3/bottom2]' //'[top2.[3], top3.middle3.bottom2]'

        differences[1].differenceType == ComparisonResult.DIFF_VALUES
        differences[1].description ==  'Values are DIFFERENT: left:(20220710) and right:(20220711)' //'[top2.[3], top3.middle3.bottom2]'


        similarities.size() == 9
        similarButDifferent.size()==1

    }
    def "Compare ignoring /created"() {
        given:
        String label = 'Compare unit test'
        Pattern ignoreValues = ~/\/created.*/
        BaseComparator comparator = new BaseComparator(label, LEFT_MAP, RIGHT_MAP, ignoreValues)

        when:
        CompareJsonObjectResults results = comparator.compare()
        def similarButDifferent = results.similarities.findAll{it.differenceType==ComparisonResult.SIMILAR}

        then:
        results.leftOnlyKeys == null

        results.rightOnlyKeys.size() == 2

        results.sharedKeys.size() == 10

        results.differences.size() == 2
        results.differences[0].differenceType == ComparisonResult.DIFF_RIGHT_ONLY
        results.differences[0].description == '[/top2/3, /top3/middle3/bottom2]'

        results.similarities.size() == 9
        similarButDifferent.size()==1
        similarButDifferent[0].description == 'Ignore value differences==true, objects have same class, so are SIMILAR: left str:(20220710) and right str:(20220711)'
    }

}
