package com.lucidworks.ps.compare


import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/12/22, Sunday
 * @description:
 */

class BaseComparatorTest extends Specification {
    static final Map LEFT_MAP = [
            top1: 'first top node',
            top2: [1, 2, 3],
            top3: [
                    middle1: 'top3:middle1',
                    middle2: [4, 5, 6],
                    middle3: [
                            bottom1: 'top3:middle3:bottom1'
                    ]]]
    static final Map RIGHT_MAP = [
            top1: 'first top node',
            top2: [1, 2, 3, 4],
            top3: [
                    middle1: 'top3:middle1 plus change',
                    middle2: [4, 5, 6],
                    middle3: [
                            bottom1: 'top3:middle3:bottom1',
                            bottom2: 'new bottom element']]]


    def "Compare"() {
        given:
        BaseComparator comparator = new BaseComparator(LEFT_MAP, RIGHT_MAP,)

        when:
        CompareObjectsResults results = comparator.compare('GenericObjectMap')

        then:
        results.leftOnlyKeys == null

        results.rightOnlyKeys.size() == 2
    }
}
