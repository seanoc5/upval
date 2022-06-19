package com.lucidworks.ps.compare

import com.lucidworks.ps.fusion.Application
import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/18/22, Saturday
 * @description:
 */

class FusionApplicationComparatorTest extends Specification {

    def "minimal fusion app export comparison via FusionApplicationComparator"(){
        given:
        File leftAppZip = new File(getClass().getResource('/apps/test.f5.partial.zip').toURI())
        Application leftApp = new Application(leftAppZip)

        File rightAppZip = new File(getClass().getResource('/apps/test.f5.partial.modified.zip').toURI())
        Application rightApp = new Application(rightAppZip)

        FusionApplicationComparator comparator = new FusionApplicationComparator(leftApp, rightApp)

        when:
        def results = comparator.compare()

        then:
        results

    }
}
