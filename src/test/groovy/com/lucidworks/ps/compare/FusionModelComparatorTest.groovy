package com.lucidworks.ps.compare

import com.lucidworks.ps.model.fusion.Application
import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   7/12/22, Tuesday
 * @description:
 */

class FusionModelComparatorTest extends Specification {
    def "Compare test app and slightly modified test app"() {
        given:
        File leftAppZip = new File(getClass().getResource('/apps/test.f5.partial.zip').toURI())
        Application leftApp = new Application(leftAppZip)

        File rightAppZip = new File(getClass().getResource('/apps/test.f5.partial.modified.zip').toURI())
        Application rightApp = new Application(rightAppZip)
        List<String> things = ['collections', 'indexPipelines']

        when:
        FusionModelComparator comparator = new FusionModelComparator(leftApp, rightApp, things, '.*(created|modified)')

        then:
        comparator.collectionComparisons.size() == 10

    }
}
