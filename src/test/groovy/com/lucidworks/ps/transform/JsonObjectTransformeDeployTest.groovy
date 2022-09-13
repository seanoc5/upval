package com.lucidworks.ps.transform

import com.lucidworks.ps.model.fusion.Application
import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/24/22, Friday
 * @description: check transform operations
 */

/**
 * @ deprecated ?? revisit to see if there is anything current/useful here...
 */
class JsonObjectTransformeDeployTest extends Specification {
    /** simple source map/json to test basic transforms */
    Map srcMap = [id           : 'srcId', name: 'srcName',
                  srcProperties: [prop1: 1, prop2: 'fooProperty 2']
    ]

    def "should change leaf node values with JsonObject"() {
        given:
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap)
        def rules = [
                copy: [
                        [from    : ~/src/, to: 'acme'],
                        [from    : ~/foo/, to: 'bar'],
                ],
        ]

        when:
        def updatedObjects = transformer.transform(rules)

        then:
        updatedObjects.size() == 1
        updatedObjects.copyResults.size() == 3
        ((Map)updatedObjects.copyResults[0]).keySet()[0].toString()=='/id'
        ((Map)updatedObjects.copyResults[0]).values()[0].toString()=='acmeId'
    }
    def "should change leaf node values to variables with JsonObject"() {
        given:
        JsonObjectTransformer transformer = new JsonObjectTransformer(srcMap)
        def rules = [
                copy: [[from    : ~/^src.*/, to: '\${foundry.ta.sourceName}'],],
        ]

        when:
        def updatedObjects = transformer.transform(rules)

        then:
        updatedObjects.size() == 1
        updatedObjects.copyResults.size() == 3
        ((Map)updatedObjects.copyResults[0]).keySet()[0].toString()=='/id'
        ((Map)updatedObjects.copyResults[0]).values()[0].toString()=='acmeId'
    }


    def "should find expected number of matching rename DC_Large objects.json items"() {
        given:
        File appZip = new File(getClass().getResource('/apps/DC_Large/DC_Large.zip').toURI())
        Map objectsMap = Application.getObjectsJsonMap(appZip)
        def ksFoo = objectsMap.keySet()     // hack to wake up lazy map...?
        JsonObjectTransformer transformer = new JsonObjectTransformer(objectsMap, objectsMap)
        String srcPath = '.*'       // look across all source items
        String srcValPattern = '~DC_Large'      // use tilde '~' to signify "contains" matching (and imply find/replace)
        Map<String, Object> srcFlatPaths = transformer.srcFlatpaths

        when:
        Map items = transformer.findAllItemsMatching('.*', srcValPattern, srcFlatPaths)
        Set itemKeys = items.keySet()


        then:
        srcFlatPaths.size() == 8477
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

    def "should rename DC_Large objects.json items"() {
        given:
        File appZip = new File(getClass().getResource('/apps/DC_Large.zip').toURI())
        Map objectsMap = Application.getObjectsJsonMap(appZip)
        Map destMap = objectsMap.clone()
        assert destMap == null
        // todo -- do we need to do something for groovy lazymaps? seems they are empty until accessed? try simple keyseet access here to get a good clone...?
        Set srcKeys = objectsMap.keySet()
        destMap = objectsMap.clone()
        assert destMap.keySet().toArray() == ['objects', 'properties', 'metadata']
        JsonObjectTransformer transformer = new JsonObjectTransformer(objectsMap, destMap)
        def rules = [copy: [
                [sourcePath           : '.*',
                 sourceItemPattern    : '~DC_Large',
                 destinationExpression: 'Acme_DigiCommerce'],
        ],]

        when:
        def updatedObjects = transformer.transform(rules)

        then:
        updatedObjects instanceof Map
    }

//    def "should transform app export"(){
//        given:
//        File appZip = new File(getClass().getResource('/apps/DC_Large/DC_Large.zip').toURI())
//        Application app = new Application(appZip)
//
//        when:
//
//
//    }
}
