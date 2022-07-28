package com.lucidworks.ps.transform

import com.lucidworks.ps.model.fusion.Blobs
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   7/27/22, Wednesday
 * @description:
 */

class BlobsTransformerTest extends Specification {
    def "check test folder for simple transform results"(){
        given:
        Map rules = [:]
        rules.rename = [
                [namePattern: 'DC_Large', replacement: 'AcmeDigicommerce'],
                [namePattern: ~/TYPEAHEAD/, replacement: 'acme_ta'],
                [namePattern: 'RESPONSE_TA', replacement: 'response_ta'],
                [namePattern: 'CAT_ID_MAPPING', replacement: 'category_mapping'],
        ]
        Path testFolder = Paths.get(getClass().getResource('/blobs/complex/').toURI())
        Blobs blobsWrapper = new Blobs('spock-test', testFolder)

        when:
        BlobsTransformer transformer = new BlobsTransformer(blobsWrapper)
        def foo = transformer.transform(rules)

        then:
        blobsWrapper.srcItems.size() == 18

    }
}
