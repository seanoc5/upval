package misc

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.internal.JsonContext
import com.lucidworks.ps.transform.JsonObject
import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * experimenting with jayway and configslurper
 */
class JaywayConfigSlurperTypeAheadTest extends Specification {

    def "basic TA configslurper"() {
        given:
        ConfigSlurper configSlurper = new ConfigSlurper()       // could do setBinding() here to pass in variables from cli
        URL cfgLocation = getClass().getResource('/configs/configTypeAheadJayway.groovy')       // could pass this in, could be a URL or full filepath
        File cfgFile = new File(cfgLocation.toURI())
        def cfgPath = cfgFile.absolutePath      //for debugging easy -- delete me?
        assert cfgFile.exists()     // explicity sanity check, better message if file not found??

        when:
        ConfigObject config = configSlurper.parse(cfgLocation)
        def sideCollection = config.objects.collections.sidecar
        def mainIdxp = config.objects.indexPipelines.main
        def signalsQryp = config.objects.queryPipelines.signalsHistory

        then:
        config instanceof ConfigObject
        mainIdxp instanceof Map
        mainIdxp.id == '${baseId}_IPL'      // not transformed (yet), still has placeholder var
        sideCollection.id == config.variables.baseId        // transformed by configslurper
    }


    def "jayway transform with configslurper"() {
        given:
        ConfigObject config = new ConfigSlurper().parse(getClass().getResource('/configs/configTypeAheadJayway.groovy'))
        Map objects = config.objects

        Map variables = [
                FEATURE_NAME     : FEATURE_NAME,
                APP              : APP,
                COLLECTION       : COLLECTION,
                ZKHOST           : ZKHOST,
                SIGNALS_AGGR_COLL: SIGNALS_AGGR_COLL,
                TYPE_FIELD_1     : TYPE_FIELD_1,
                TYPE_FIELD_2     : TYPE_FIELD_2,
                numShards        : numShards,
                replicationFactor: replicationFactor,
                maxShardsPerNode : maxShardsPerNode,
                baseId           : baseId,
        ]
        // String template for the externally sourced pipelines
        String output = new groovy.text.SimpleTemplateEngine().createTemplate(config.indexJson.text).make(variables).toString()


        JsonContext jsonContext = JsonPath.parse(objects)
//        def jaywayTransforms = config.transforms
        def updates = jsonContext.read('$..updates')

        when:
        jsonContext.set('$.dataSources.fileUpload.id', 'my-test-id')
        jsonContext.delete('$..updates')
        def postTransformUpdates = jsonContext.read('$..updates')

        then:
        objects.dataSources.fileUpload.id == 'my-test-id'
        postTransformUpdates.size() == 0
        objects.queryProfiles.size() == 3
    }



    def "jayway transform with configslurper typeAheadPackage"() {
        given:
        ConfigObject config = new ConfigSlurper().parse(getClass().getResource('/configs/typeAheadPackage.groovy'))
        Map objects = config.objects


        JsonContext jsonContext = JsonPath.parse(objects)
        def updates = jsonContext.read('$..updates')

        when:
        jsonContext.set('$.dataSources[0].id', 'my-test-id')
//        jsonContext.delete('$..updates')
//        jsonContext.set('$.indexPipelines[0].id', 'myjaywayId')
        jsonContext.set('$.queryPipelines[0].id', 'myjaywayId')
//        def postTransformUpdates = jsonContext.read('$..updates')

        then:
        objects.dataSources[0].id == 'my-test-id'
//        postTransformUpdates.size() == 0
        objects.queryProfiles.size() == 3
    }

    def "transform with configslurper typeAheadPackage externalized"() {
        given:
        ConfigObject config = new ConfigSlurper().parse(getClass().getResource('/configs/typeAheadPackageExternalized.groovy'))
        Map objects = config.objects

        when:
        def idxp = objects.indexPipelines
        def qryp = objects.queryPipelines
        def coll = objects.collections
        def blobs = objects.blobs

        then:
        objects.dataSources[0].id == 'my-test-id'
//        postTransformUpdates.size() == 0
        objects.queryProfiles.size() == 3
    }


    // todo -- figure out how to not evaluate multi-line elements (like js, and js conditions)
    def "test not evaluating strings"() {
        given:
        String a = "line \${one}\\\\nline two"
        String b = 'line ${one}\\\\nline two'
        String c = '''line ${one}\\\\nline two'''
        groovy.text.SimpleTemplateEngine ste =  new groovy.text.SimpleTemplateEngine()

        when:
        String aa = ste.createTemplate(a).make([one:'one']).toString()
        String bb = ste.createTemplate(b).make([one:'one']).toString()
        def bblines = bb.readLines()
        String cc = ste.createTemplate(c).make([one:'one']).toString()
        def cclines = cc.readLines()

        then:
        aa instanceof String
        aa.readLines().size() == 2          // this is desired parsing/eval
        b.readLines().size() == 1           // unparsed, stays as one line
        c.readLines().size() == 1           // tunparsed, stays as one line

        bblines.size() == 1          // this is desired parsing/eval - but not working (yet)
        cclines.size() == 1          // this is desired parsing/eval - but not working (yet)
    }

    def "parse indexpipeline externalized file"() {
        given:
        String idxpTemplate = new File('./src/test/resources/components/typeahead/indexpipeline.main.v1.json').text
        String jsUnwantedTerms = JsonObject.escapeSource(new File('./src/test/resources/components/typeahead/excludeUnwantedTerms.js').text)
        String idxpJson = new groovy.text.SimpleTemplateEngine().createTemplate(idxpTemplate).make([APP: 'MyApp', baseId: 'MyApp_Typeahead', jsUnwantedTerms:jsUnwantedTerms]).toString()
//        File temp = new File("/Users/Sean/work/temp.json")
//        temp.text = idxpJson
        // todo -- work out proper escaping -- currently terms.replace() have been removed for convenience...
        def idxp = new JsonSlurper().parseText(idxpJson)

        ConfigObject config = new ConfigSlurper().parse(getClass().getResource('/configs/typeAheadPackage.groovy'))
        Map objects = config.objects


        JsonContext jsonContext = JsonPath.parse(objects)
        def pretransformUpdates = jsonContext.read('$..updates')

        when:
        jsonContext.set('$.dataSources[0].id', 'my-test-id')
        jsonContext.delete('$..updates')
        def posttransformUpdates = jsonContext.read('$..updates')

//        jsonContext.set('$.indexPipelines[0].id', 'myjaywayId')
        jsonContext.set('$.queryPipelines[0].id', 'myjaywayId')
//        def postTransformUpdates = jsonContext.read('$..updates')

        then:
        objects.dataSources[0].id == 'my-test-id'
//        postTransformUpdates.size() == 0
        objects.queryProfiles.size() == 3
        pretransformUpdates.size() == 3
        posttransformUpdates.size() == 0
    }


}
