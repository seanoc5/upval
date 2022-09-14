package com.lucidworks.ps.transform


import spock.lang.Specification

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/26/22, Sunday
 * @description:
 */

/**
 * test class for basic JsonSlurper object navigation (get nodes), and transformations (set nodes, escape/unescape string values, e.g. embedded javascript)
 */
class JsonObjectPackagingTest extends Specification {


    Map idxpMinimal = [id      : "Components_TYPEAHEAD_DW_IPL_v4",
                       "stages": [
                               [
                                       "id"                 : "mjsi-Components_TYPEAHEAD_DW-MlF",
                                       "ref"                : "lib/index/FusionServiceLib.js",
                                       "type"               : "managed-js-index",
                                       "skip"               : false,
                                       "label"              : "MJS: load FusionServices.js",
                                       "secretSourceStageId": "b3736ea6-c8a8-4180-950a-1c5f47a80f49"
                               ],

                               [
                                       "id"                 : "jsq-Components_TYPEAHEAD_DW-ORHD",
                                       "script"             : "function (request, response, ctx) {\n \n  var q = request.getFirstParam('q')\n  request.putSingleParam('q', \"ta_type:history && \" + q)\n}",
                                       "shareState"         : true,
                                       "type"               : "javascript-query",
                                       "skip"               : false,
                                       "label"              : "Only Return History Documents",
                                       "condition"          : "request.hasParam(\"historyOnly\") && request.getFirstParam(\"historyOnly\").equals(\"true\");\n" +
                                               "// This stage is used to the make the results only history documents. The Components_TYPEAHEAD_DC_history_QPF will send a parameter historyOnly=true",
                                       "secretSourceStageId": "2e25eeaa-ac2b-4e42-8603-39dea784d9ff"
                               ],
                       ],
    ]


    def "should find items with component and app names via JsonObjectTransforme"() {
        given:
        JsonObjectTransformer transformer = new JsonObjectTransformer(idxpMinimal)
        String appName = 'Components'
        String pkgName = 'TYPEAHEAD_DW'

        when:
        Map foundAppEntries = transformer.findAllItemsMatching('', appName, idxpMinimal)
        Map foundPkgEntries = transformer.findAllItemsMatching('', pkgName, idxpMinimal)
//        Map foundPkgLeafs = transformer.find('', pkgName, idxpMinimal)

        then:
        foundAppEntries instanceof Map
        foundAppEntries.size() == 2
        foundPkgEntries instanceof Map
        foundPkgEntries.size() == 2
    }

    def "should replace component and app names with import variables"() {
        given:
        JsonObjectTransformer transformer = new JsonObjectTransformer(idxpMinimal)

        def rules = [
                copy: [
                        [from: ~/Components/, to  : 'ta.appplicationName'],      // components is source appname, create variable to prompt for AppName on import
                        [from: ~/TYPEAHEAD_DW/, to  : 'ta.label'],               // create variable for label/name of this typeahead deployment
                ],]

        when:
        def results = transformer.transform(rules)


        then:
        results instanceof Map
        results.copyResults instanceof List
        results.copyResults.size() == 7
        idxpMinimal.id
    }


/*
    @Unroll
    def "basic SET values sanity checks"() {
        given:
        Map newMapToAdd = [bizz: 1, buzz: 2]            // testing being able to add a new structure, not just a leaf node (primative value)
        String valToSet = 'my new value here'           // using a variable (string) for setting new values, makes testig easier/more consisten
        // check if element exists initially, same as: srcMap['a']['three'] -- GPath shorthand
        // element does not exist initially
        assert srcMap.a.three == null
        assert srcMap.b[0] == 'three'

        when:
        // set a List element by index:
        Map<String, Object> b0 = JsonObject.setObjectNodeValue(srcMap, '/b/0', valToSet)
        def b0Key = b0.keySet()[0]

        // add a new element (map entry) to existing parent (sub-map: a)
        Map<String, Object> resultThree = JsonObject.setObjectNodeValue(srcMap, '/a/three', valToSet)       // see upval JsonObject for 'slashy' string GPath/JsonPath alternative

        // create new 'top' leaf node
        Map<String, Object> resultNewLeaf = JsonObject.setObjectNodeValue(srcMap, '/newTopLeaf', valToSet)

        // create a new map (not simple primative value) in a new element (map entry)
        Map<String, Object> resultNewFourSubMap = JsonObject.setObjectNodeValue(srcMap, '/a/four', newMapToAdd)

        // check navigating through maps and lists
        Map<String, Object> resulCompositekey1 = JsonObject.setObjectNodeValue(srcMap, '/compositeList/2/submapkey1', valToSet)

        then:
        b0.get(b0Key) == valToSet                      // successfully set list element by index
        resultNewLeaf.keySet()[0] == '/newTopLeaf'           // added a new leaf node: to parent element(map) `a`
        resultNewLeaf.get(resultNewLeaf.keySet()[0]) == valToSet

        srcMap.newTopLeaf == valToSet

        resultThree.keySet()[0] == '/a/three'
        resultThree.get(resultThree.keySet()[0]) == valToSet
        srcMap.a.three == valToSet          // element `a.three` does exist now
        resultNewFourSubMap.toString() == "[/a/four:{bizz=1, buzz=2}]"      // todo can we force returning the actual object rather than the toString() of the object?
        srcMap.a.four.bizz == 1
        srcMap.a.four.buzz == 2
    }

    def "basic SET values sanity checks with datatables"() {
        when:
        def  origValue = JsonObject.getObjectNodeValue(srcMap, path, separator)
        Map<String, Object> result = JsonObject.setObjectNodeValue(srcMap, path, v, separator)

        then:
        checkValue == result.get(result.keySet()[0])

        where:
        path                           | separator | v           | checkValue
        '/newTopLeaf'                  | '/'       | "vnew"      | 'vnew'
        '/a'                           | '/'       | "vnew"      | 'vnew'
        '/a/two'                       | '/'       | 'new value' | 'new value'
        '/b/0/'                        | '/'       | 'new value' | 'new value'
        '/componsiteList/2/submapkey1' | '/'       | 'new value' | 'new value'
    }

    def "should remove items by path"() {
        Map m = [a: [b: [c: [d1: 'delete me', d2: 2]]]]
        def flatties = JsonObject.flattenWithLeafObject(m)
        String childPath = '/a/b/c/d1'
        def itemToRemove = JsonObject.getObjectNodeValue(m, childPath)
        String parentPath = JsonObject.getParentPath(childPath)
        Map parentItem = JsonObject.getObjectNodeValue(m, parentPath)

        when:
        assert parentItem.keySet().toList() == ['d1', 'd2']
        def removedItem = JsonObject.removeItem(childPath, m)
//        def parentItem = JsonObject.getParentItem(childPath, flatties)

        then:
        removedItem[childPath] == 'delete me'
        parentItem.keySet().toList() == ['d2']
        m.a.b.c.d2 == 2
        m.a.b.c.d1 == null

    }
*/

}


