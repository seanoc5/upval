package com.lucidworks.ps.upval.mapping

import org.apache.log4j.Logger

class ObjectTransformer {
    Logger log = Logger.getLogger(this.class.name);
    String pathSeparator = '\\|'

    ObjectTransformer() {
        log.info "Construct new SharepointOptimizedTransformer..."
    }


    /**
     * get Map value by path (json?gpath?...???)
     *
     * @param objPath
     * @param map
     * @return
     */
    def getByMapPath(String objPath, Map<String, Object> map) {
        def thing
        List<String> pathParts = objPath.split(pathSeparator)
        log.debug "\t\tGET by json path (${pathParts.size()} parts): $objPath"
        if (pathParts.size() == 1) {
            thing = map.get(pathParts[0])
        } else if (pathParts.size() == 2) {
            thing = map.get(pathParts[0]).get(pathParts[1])
//            thing = ((MapEntry)map.get(pathParts[0])).get(pathParts[1])
//            def foo = ((Map)map.get(pathParts[0])).entrySet()
//            log.info "foo? $foo"
        } else if (pathParts.size() == 3) {
            thing = map.get(pathParts[0]).get(pathParts[1]).get(pathParts[2])
        }
        if (thing) {
            log.debug "\t\tGOT jsonpath '$objPath' -- value:'$thing'"
        } else {
            log.debug "\t\tempty jsonpath '$objPath' -- value:'$thing'"
        }
        return thing
    }

    /**
     * update map via gpath-type navigation
     * @param mapPath
     * @param value
     * @param map
     * @return
     */
    def setByMapPath(String mapPath, def value, Map<String, Object> map) {
        log.debug "\t\tSET jsonpath($mapPath) -- value:$value"
        List<String> pathParts = mapPath.split(pathSeparator)
        if (pathParts.size() == 1) {
            map[pathParts[0]] = value
        } else if (pathParts.size() == 2) {
            map[pathParts[0]][pathParts[1]] = value
        } else if (pathParts.size() == 3) {
            map[pathParts[0]][pathParts[1]][pathParts[2]] = value
        }
    }


    def transform(String templatePath, String sourcePath, Map templateObject, Map sourceObject) {
        log.info "Testing 'transform' function (more to come): $sourcePath -> $templatePath (not showing objects...)"

    }
}
