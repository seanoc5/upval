package com.lucidworks.ps.upval

import spock.lang.Specification

class HelperSpecification extends Specification {
    Map map = null
    void setup() {
        map = [
                top1: [
                        middle1a: [bottom1a1: [subbottom1a1a: 'endleaf1'], bottom1a2: 'endleaf2',],
                        middle1b: [bottom1b1: 'endleaf3'],
                        middle1c: [[listMap1:'a'],[listMap2:'b']]
                ],
                top2: [
                        middle2a: [bottom2a1: 'endleaf1', bottom2a2: 'endleaf2'],
                        middle2b: [bottom2b1: 'endleaf3']
                ]
        ]
    }

    def "Flatten"() {
        given:

        when:
        def flatties  = Helper.flatten(map, 1)

        then:
        flatties instanceof List
        flatties.size()==8
        flatties[0] == 'top1.middle1a.bottom1a1.subbottom1a1a'
        flatties[1] == 'top1.middle1a.bottom1a2'
    }
}
