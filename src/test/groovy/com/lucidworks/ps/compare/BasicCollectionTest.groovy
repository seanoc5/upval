package com.lucidworks.ps.compare

import com.lucidworks.ps.upval.Helper
import spock.lang.Specification

class BasicCollectionTest extends Specification {

    def "Flatten"() {
        given:
        Map leftMap = [top1: 'first top node',
                       top2: [1, 2, 3],
                       top3: [
                               middle1: 'top3:middle1',
                               middle2: [4, 5, 6],
                               middle3: [
                                       bottom1: 'top3:middle3:bottom1'
                               ]]]
        Map rightMap = [top1: 'first top node',
                        top2: [1, 2, 3, 4],
                        top3: [
                                middle1: 'top3:middle1 plus change',
                                middle2: [4, 5, 6],
                                middle3: [
                                        bottom1: 'top3:middle3:bottom1',
                                        bottom2: 'new bottom element']]]

        when:
        def flatLeft = Helper.flattenPlus(leftMap)
        def flatRight = Helper.flattenPlus(rightMap)

        then:
        flatLeft.size() == 9
        flatRight.size() == 11
        flatLeft == flatRight
    }



}
