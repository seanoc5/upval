package com.lucidworks.ps.components

import com.lucidworks.ps.clients.FusionClient
import org.apache.tools.zip.ZipOutputStream
import spock.lang.Specification

//import java.util.zip.ZipOutputStream

/**
 * test suite to cover component packing operations
 */
class ComponentPackagingTest extends Specification {
    def "create simple query pipeline package with variable substitution"() {
        given:
        FusionClient fusionClient = new FusionClient('http://newmac:8764', 'sean', 'pass1234', 'test')
        def qryp = fusionClient.getQueryPipeline('test')

        when:
        Map objMap = [objects: [
                queryPipelines: [qryp]
        ]
        ]
        def newMap = fusionClient.setObjectsMetadata(objMap, '4.2.6', 'ta-foundry-newmac')
        File outFile = File.createTempFile("Compackage.test.simple", ".zip")
        println(outFile.absolutePath)
        ZipOutputStream zos = fusionClient.createImportableZipArchive(outFile.newOutputStream(), objMap)

        then:
        zos instanceof ZipOutputStream            // false??? why?
        zos.finished == true

    }

}
