package com.lucidworks.ps.transform

import com.lucidworks.ps.model.fusion.Application
import groovy.json.JsonSlurper
import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/24/22, Friday
 * @description:
 */

/**
 * @ deprecated ?? revisit to see if there is anything current/useful here...
 */
class JsonObjectTransformeDeployTATest extends Specification {
    def "ta-objects quick test"() {
        given:
        Map rules = [copy: [
                [from: 'foundry.typeahead.ZKHOST', to: 'myzk-0.myzk-headless:2181,myzk-1.myzk-headless:2181,myzk-2.myzk-headless:2181'],
//                [from: ~/($\{)?foundry.destination.SIGNALS_AGGR_COLL}?/, to: "${appName}_signals_aggr"],
//                [from: ~/($\{)?foundry.FEATURE_NAME}/, to: "${taName}"],
        ]
        ]

        URL url = getClass().getResource('/components/ta-objects.json')
        Map objectsMap = new JsonSlurper().parse(url)
        JsonObjectTransformer transformer = new JsonObjectTransformer(objectsMap)

        when:
        def results = transformer.transform(rules)


        then:
        results instanceof Map
        results.copyResults instanceof List
        results.copyResults.size() == 3


    }


    def "should transform TypeAhead sample objects"() {
        given:
        URL configUrl = getClass().getResource('/configs/configTypeAhead.groovy')
        ConfigSlurper configSlurper = new ConfigSlurper()
        String myAppName = 'MyFusionApp'
        Map cliArgs = [appName: myAppName]
        configSlurper.setBinding(cliArgs)
        ConfigObject config = configSlurper.parse(configUrl)
        assert config.appName == myAppName

        URL url = getClass().getResource('/components/ta-objects.json')
        Map objectsMap = new JsonSlurper().parse(url)
        JsonObjectTransformer transformer = new JsonObjectTransformer(objectsMap)

        when:
        def results = transformer.transform(config.transform)


        then:
        results instanceof Map
        results.copyResults instanceof List
        results.copyResults.size() == 84

        results.size() == 8477
        items.size() == 947

        // check a few psuedo-random values in the returned items map...
        // todo -- select better test conditions? more focused/important??
        itemKeys[0] == '/objects/collections/0/id'
        items[itemKeys[0]] == 'DC_Large'

        itemKeys[1] == '/objects/collections/0/solrParams/name'
        items[itemKeys[1]] == 'DC_Large'

        itemKeys[95] == '/objects/queryPipelines/3/id'
        items[itemKeys[95]] == 'DC_Large_TYPEAHEAD_QPL_v4'

        itemKeys[940] == '/objects/dataSources/1/id'
        items[itemKeys[940]] == 'DC_Large_Populate_Signals_DS'


    }

}
