package misc


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
        def mainIdxp= config.objects.indexPipelines.main
        def signalsQryp= config.objects.queryPipelines.signalsHistory

        then:
        config instanceof ConfigObject
        mainIdxp instanceof Map
        mainIdxp.id == '${baseId}_IPL'      // not transformed (yet), still has placeholder var
        sideCollection.id == config.variables.baseId        // transformed by configslurper
    }

    def "jayway transform with configslurper"() {
        given:
        ConfigObject config = new ConfigSlurper().parse(getClass().getResource('/configs/configTypeAheadJayway.groovy'))
        def jaywayTransforms = config.transforms

        when:
        def sideCollection = config.objects.collections.sidecar
        def mainIdxp= config.objects.indexPipelines.main
        def signalsQryp= config.objects.queryPipelines.signalsHistory

        then:
        config instanceof ConfigObject
        mainIdxp instanceof Map
        mainIdxp.id == '${baseId}_IPL'      // not transformed (yet), still has placeholder var
        sideCollection.id == config.variables.baseId        // transformed by configslurper
    }



}