package com.lucidworks.ps.mapping

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import org.apache.log4j.Logger

/**
 * An attempt to use built-in groovy GPath (which is great for XML, questionable for maps/lists)
 * @deprecated review status, I think jayaway is better at the moment
 */
class ObjectTransformerGPath {
    Logger log = Logger.getLogger(this.class.name);
    Map sourceMap
    Map destinationMap
    Map transformConfig
    String pathSeparator = null

    ObjectTransformerGPath(Map srcMap, Map destMap, Map transConfig, String separator = '/') {
        log.info "Constructor: (Map srcMap, Map destMap, Map transformConfig, String pathSeparator)..."
        sourceMap = srcMap
        destinationMap = destMap
        transformConfig = transConfig
        pathSeparator = separator
    }

    def transform() {
        def setRules = transformConfig.get('set')
        setRules.each { String destPath, String value ->
            def foo = setByMapPath(destPath, value, destinationMap)
            log.info "Set '$destPath' in destinationMap to value: '$value'"
        }
        log.warn "More code here???"
    }

    def transformSetValues() {
        def setRules = transformConfig.get('set')
        setRules.each { String destPath, String value ->
            Object valueToSet = evaluateValue(value)
            def foo = setValueByJsonPath(destPath, valueToSet, destinationMap)
            log.info "\t\tSet '$destPath' in destinationMap to value: '$value' -- foo:$foo"
        }
    }

    /**
     * helper function to do 'live' evaluations from config set
     *
     * todo: look at if 'objects' actually work here, or is a string representation good (better?) for setting json values...?
     * @param value
     * @return
     */
    public Object evaluateValue(String value) {
        String valueToSet = null
        if (value.contains('$')) {
            // http://www.groovyconsole.appspot.com/edit/22004?execute
            log.debug "\t\tprepare to evaluate me...: $value"
            valueToSet = Eval.me(/"$value"/)
            log.info "\t\tEvaluated gstring ($value) ==> $valueToSet"
        } else {
            valueToSet = value
        }
        valueToSet
    }

    def getValueByJsonPath(String jsonPath, Map<String, Object> map) {
        def thing = null
        try {
            thing = JsonPath.read(map, jsonPath)
        } catch (PathNotFoundException pnfe) {
            log.warn "Path not found: $pnfe"
        }
        return thing
    }

    def setValueByJsonPath(String jsonPath, def value, Map<String, Object> map) {
        log.debug "\t\tSet value by json path('$jsonPath') value:($value) in map with keys: (${map.keySet()} "
        def foo = null
        try {
            DocumentContext context = JsonPath.parse(map)
            foo = context.set(jsonPath, value)
        } catch (PathNotFoundException pnfe) {
            log.warn "Path ($jsonPath) not found: $pnfe"
        }

        return foo
    }

    /**
     * check proper syntax
     *
     * expecting to improve flexibility like wildcards and such
     * @param mapPath
     * @return
     */
    boolean isValidMapPath(String mapPath) {
        boolean valid = false
        if (mapPath.startsWith('/')) {
            log.debug "starts with slash, good... $mapPath"
            valid = true

        } else {
            log.warn "Map Path does not START with slash (/), please use correct format: start with initial slash, and separate out map children with slashes"
        }
        return valid
    }

    /**
     * get Map value by path (json?gpath?...???)
     *
     * @param mapPath
     * @param map
     * @return
     */
    def getValueByMapPath(String mapPath, Map<String, Object> map) {
        def thing
        log.debug "ge value based on mappath: $mapPath"
        if (isValidMapPath(mapPath)) {

            List<String> pathParts = mapPath.split(pathSeparator)
            log.debug "\t\tGET by json path (${pathParts.size()} parts): $mapPath"
            if (pathParts.size() == 1) {
                log.warn "Invalid path parts $pathParts"
            } else if (pathParts.size() == 2) {
                thing = map.get(pathParts[1])
//            thing = ((MapEntry)map.get(pathParts[0])).get(pathParts[1])
//            def foo = ((Map)map.get(pathParts[0])).entrySet()
//            log.info "foo? $foo"
            } else if (pathParts.size() == 3) {
                thing = map.get(pathParts[1]).get(pathParts[2])
            } else if (pathParts.size() == 4) {
                thing = map.get(pathParts[1]).get(pathParts[2]).get(pathParts[3])
            }
            if (thing) {
                log.debug "\t\tGOT jsonpath '$mapPath' -- value:'$thing'"
            } else {
                log.debug "\t\tempty jsonpath '$mapPath' -- value:'$thing'"
            }
        } else {
            log.warn "Object Path not valid: $mapPath"
        }
        return thing
    }

    /**
     * work with map entries so we can pass an object that is easily update (tyipcally the "destination" map, not the source)
     *
     * todo: move this to recursive approach, handle lists, and other unknown goodies...
     * @param mapPath
     * @param map
     * @return map Entry that should allow for 'direct' updating, rather than refinding the map entry to set it
     */
/*    Map.Entry<String,Object> getMapEntryByPath(String mapPath, Map<String, Object> map) {
        def thing
        log.debug "get Map Entry based on mappath: $mapPath"
        if(isValidMapPath(mapPath)) {

            List<String> pathParts = mapPath.split(pathSeparator)
            log.debug "\t\tGET by json path (${pathParts.size()} parts): $mapPath"
            if (pathParts.size() == 1) {
                log.warn "Invalid path parts $pathParts"
            } else if (pathParts.size() == 2) {
                String key = pathParts[1]
                thing = map.find { Map.Entry entry -> entry.key == key }
            } else if (pathParts.size() == 3) {
                thing = map.get(pathParts[1]).get(pathParts[2])
            } else if (pathParts.size() == 4) {
                thing = map.get(pathParts[1]).get(pathParts[2]).get(pathParts[3])
            }
            if (thing) {
                log.debug "\t\tGOT jsonpath '$mapPath' -- value:'$thing'"
            } else {
                log.debug "\t\tempty jsonpath '$mapPath' -- value:'$thing'"
            }
        } else {
            log.warn "Object Path not valid: $mapPath"
        }
        return thing
    }*/


    /**
     * update map via gpath-type navigation
     *
     * Note: assuming path separator MUST be first character (revise later?), so first path-split part is blank
     * @param mapPath
     * @param value
     * @param map
     * @return
     */
    def setByMapPath(String mapPath, def value, Map<String, Object> map) {
        log.info "\t\tSET jsonpath($mapPath) -- value:$value"
        String valueToSet = null
        if (value.contains('$')) {
            // http://www.groovyconsole.appspot.com/edit/22004?execute
            log.debug "prepare to evaluate me...: $value"
            valueToSet = Eval.me(/"$value"/)
            log.info "Evaluated gstring ($value) ==> $valueToSet"
        } else {
            valueToSet = value
        }
        def thingtoSet = null
        List<String> pathParts = mapPath.split(pathSeparator)
        if (pathParts.size() == 2) {
            thingtoSet = map[pathParts[1]]
            map[pathParts[1]] = valueToSet
        } else if (pathParts.size() == 3) {
            thingtoSet = map[pathParts[1]][pathParts[2]]
            map[pathParts[1]][pathParts[2]] = valueToSet
        } else if (pathParts.size() == 4) {
            thingtoSet = map[pathParts[1]][pathParts[2]][pathParts[3]]
            map[pathParts[1]][pathParts[2]][pathParts[3]] = valueToSet
        } else if (pathParts.size() == 5) {
            thingtoSet = map[pathParts[1]][pathParts[2]][pathParts[3]][pathParts[4]]
            map[pathParts[1]][pathParts[2]][pathParts[3]][pathParts[4]] = valueToSet
        } else {
            log.warn "Found invalid path parts (too deep?): $pathParts"
        }
        log.debug "thing to set: $thingtoSet"
    }


}
