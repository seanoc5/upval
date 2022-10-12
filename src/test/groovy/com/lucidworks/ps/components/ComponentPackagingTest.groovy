package com.lucidworks.ps.components

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.transform.JsonObjectTransformer
import org.apache.tools.zip.ZipOutputStream
import spock.lang.Specification
//import java.util.zip.ZipOutputStream

/**
 * test suite to cover component packing operations
 * set environment variables fuser and fpass appropriately to override placeholder values (see below)
 */
class ComponentPackagingTest extends Specification {
    // NOTE: this breaks best practice for unit tests, but we are pulling connection info from the environment, so set these values in the system env variables when running these tests, otherwise expect them to fail...
    Map<String, String> env = System.getenv()
    String furl = env.furl ?: 'http://foundry.lucidworksproserve.com:6764'
    String fuser = env.fuser ?: 'admin'
    String fpass = env.fpass ?: 'password123'
    String qryp = env.qryp ?: 'Components_TYPEAHEAD_DW_QPL_v4'
    FusionClient fusionClient = new FusionClient(furl, fuser, fpass)

    def "create simple query pipeline package WITHOUT variable substitution"() {
        given:
        def qryp = fusionClient.getQueryPipeline(qryp)
        // setting 'destination' fusion version to 4.2.6, to test importing into local (non F5) fusion, adjust as desired
        Map<String, Object> metdata = fusionClient.createtObjectsMetadata('4.2.6', 'ta-foundry-v4')
        Map objMap = [
                objects : [queryPipelines: [qryp]],
                metadata: metdata,
        ]

        when:
        File outFile = File.createTempFile("Compackage.test.simple", ".zip")
        println "Created temporary output file: ${outFile.absolutePath}"           // print out temp file path in case tester wants to actually import into a running fusion for sanity check...
        ZipOutputStream zos = fusionClient.createImportableZipArchive(outFile.newOutputStream(), objMap)

        then:
        zos instanceof ZipOutputStream            // false??? why?
        zos.finished == true
    }

    def "JsonObjectTransformer query pipeline package WITH variable substitution"() {
        given:
        def qrypMap = fusionClient.getQueryPipeline(qryp)
        Map<String, Object> metdata = fusionClient.createtObjectsMetadata('4.2.6', 'ta-foundry-with-vars')
        Map objMap = [
                objects : [queryPipelines: [qrypMap]],
                metadata: metdata,
        ]
//        JsonObject jsonObject = new JsonObject(objMap)
        JsonObjectTransformer transformer = new JsonObjectTransformer(objMap)

        Map variables = [
                'foundry.FEATURE_NAME': [from: 'TYPEAHEAD_DW', default: 'typeahead_dw'],
                "foundry.destination.SIGNALS_AGGR_COLL": [from: "SIGNALS_AGGR_COLLECTION", default:'SourceApp_signals_aggr'],
                "foundry.destination.APP": [from:"Components", default:'__DestAppHere__'],
                "foundry.destination.COLLECTION": [from:'COLLECTION', default:"typeahead"],
//                "foundry.typeahead.ZKHOST": [to:"ZOOKEEPER-0.ZOOKEEPER-headless:2181,ZOOKEEPER-1.ZOOKEEPER-headless:2181,ZOOKEEPER-2.ZOOKEEPER-headless:2181",],
//                "foundry.typeahead.TYPE_FIELD_1": [to:"TYPE_FIELD_1",],
//                "foundry.typeahead.TYPE_FIELD_2": "TYPE_FIELD_2",
//                "foundry.typeahead.TYPE_FIELD_3": "TYPE_FIELD_3",
//                "foundry.typeahead.TYPE_FIELD_4": "TYPE_FIELD_4",
//                "foundry.typeahead.TYPE_FIELD_5": "TYPE_FIELD_5",
        ]
        Map<String, String> outputVariables = transformer.performVariableSubstitution(variables)

        when:
        File outFile = File.createTempFile("Compackage.test.with-variables", ".zip")
        println "Created temporary output file: ${outFile.absolutePath}"           // print out temp file path in case tester wants to actually import into a running fusion for sanity check...

        ZipOutputStream zos = fusionClient.createImportableZipArchive(outFile.newOutputStream(), objMap)

        then:
        zos instanceof ZipOutputStream            // false??? why?
        zos.finished == true
        outputVariables.size()==variables.size()
    }

    def "ObjectTransformerJayway query pipeline package WITH variable substitution"() {
        given:
        def qrypMap = fusionClient.getQueryPipeline(qryp)
        Map<String, Object> metdata = fusionClient.createtObjectsMetadata('4.2.6', 'ta-foundry-with-vars')
        Map objMap = [
                objects : [queryPipelines: [qrypMap]],
                metadata: metdata,
        ]
//        JsonObject jsonObject = new JsonObject(objMap)
        JsonObjectTransformer transformer = new JsonObjectTransformer(objMap)

        Map variables = [
                'foundry.FEATURE_NAME': [from: 'TYPEAHEAD_DW', default: 'typeahead_dw'],
                "foundry.destination.SIGNALS_AGGR_COLL": [from: "SIGNALS_AGGR_COLLECTION", default:'SourceApp_signals_aggr'],
                "foundry.destination.APP": [from:"Components", default:'__DestAppHere__'],
                "foundry.destination.COLLECTION": [from:'COLLECTION', default:"typeahead"],
//                "foundry.typeahead.ZKHOST": [to:"ZOOKEEPER-0.ZOOKEEPER-headless:2181,ZOOKEEPER-1.ZOOKEEPER-headless:2181,ZOOKEEPER-2.ZOOKEEPER-headless:2181",],
//                "foundry.typeahead.TYPE_FIELD_1": [to:"TYPE_FIELD_1",],
//                "foundry.typeahead.TYPE_FIELD_2": "TYPE_FIELD_2",
//                "foundry.typeahead.TYPE_FIELD_3": "TYPE_FIELD_3",
//                "foundry.typeahead.TYPE_FIELD_4": "TYPE_FIELD_4",
//                "foundry.typeahead.TYPE_FIELD_5": "TYPE_FIELD_5",
        ]
        Map<String, String> outputVariables = transformer.performVariableSubstitution(variables)

        when:
        File outFile = File.createTempFile("Compackage.test.with-variables", ".zip")
        println "Created temporary output file: ${outFile.absolutePath}"           // print out temp file path in case tester wants to actually import into a running fusion for sanity check...

        ZipOutputStream zos = fusionClient.createImportableZipArchive(outFile.newOutputStream(), objMap)

        then:
        zos instanceof ZipOutputStream            // false??? why?
        zos.finished == true
        outputVariables.size()==variables.size()
    }

    def "create moderate query and index pipeline with variable substitution"() {
        given:
        def qryp = fusionClient.getQueryPipeline(qryp)
        Map<String, Object> metdata = fusionClient.createtObjectsMetadata('4.2.6', 'ta-foundry-newmac')
        Map objMap = [
                objects : [queryPipelines: [qryp]],
                metadata: metdata,
        ]

        when:
        File outFile = File.createTempFile("Compackage.test.simple", ".zip")
        println "Created temporary output file: ${outFile.absolutePath}"           // print out temp file path in case tester wants to actually import into a running fusion for sanity check...

        ZipOutputStream zos = fusionClient.createImportableZipArchive(outFile.newOutputStream(), objMap)

        then:
        zos instanceof ZipOutputStream            // false??? why?
        zos.finished == true

    }

}
