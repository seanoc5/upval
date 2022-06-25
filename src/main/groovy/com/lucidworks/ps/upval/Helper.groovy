package com.lucidworks.ps.upval


import org.apache.log4j.Logger

//import java.security.InvalidParameterException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * General helper class
 * todo consider refactoring flattening operations to a more obvious classname (low-priority as it is a low-level op, not big picture processing)
 */
class Helper {
    static Logger log = Logger.getLogger(this.class.name);

    /**
     * Get the value of the item with the given JsonSlurped object (typically this would be a simple value, so no easy way to get a discrete object to update in place
     * Probably do a get to check for a value (optional?), followed by a setJsonObjectNode for any update process (promotion/migration...)
     * @param Map srcMap - the map (object?) to travers baesd on path param
     * @param path - the string defining the path through the jsonslurped object (maps/collections/leafNodes with primative values)
     * @param separator - string separator to split path on, defaults to slash '/'
     * @param valToSet
     * @return the (primitive?) value of the 'thing' in the JsonObject path (can't assume it is an object that we can update in place, need to call setJsonObjectNode if you want to update)
     */
    static def getObjectNodeValue(Map srcMap, String path, String separator = '/') {
        log.info "Process path: [$path] in src:$srcMap "
        List<String> segments = path.split(separator)
        segments.remove(0)
        int numSegments = segments.size()
        def element = srcMap
        // loop through path segments, stop when reaching the last element, or a (parent) segment is null
        for (int depth = 0; depth < numSegments && element; depth++) {
            String seg = segments[depth]
            def child = getElement(seg, element)
            log.debug "\t\t$depth - $seg) check if it exists... "
            if (!child) {
                log.info "\t\t${depth}) encountered MISSING segment: $seg -> return null, exit for loop..."
            } else {
                log.debug "\t\t$depth) Found segment ($seg) -> $element"
            }
            element = child

        }
        log.info "return Path ($path) element (value): $element "
        return element
    }

    /**
     * testing some abstraction for getting an element without knowing colletion or map (duck typing...?)
     * @param key
     * @param map
     * @return
     */
    static def getElement(String key, Map map) {
        map[key]
    }

    static def getElement(def segment, Collection collection) throws IllegalArgumentException {
        Integer index = null
        def element
        if (segment instanceof String) {
            index = Integer.parseInt(segment)
        } else if (segment instanceof Integer) {
            index = (Integer) index
        } else {
            String msg = "Segment param ($segment) type (${segment.getClass().simpleName}) not understood (expected int-type var)"
            throw new IllegalArgumentException(msg)
        }
        if (index < collection.size()) {
            element = collection[index]
        } else {
            log.warn "\t\tAsked for segment($segment) element with index > max index (${collection.size() - 1}) collection has (${collection.size()}) elements (0-based), nothing to get, return null"
            element = null
        }

        element
    }

    /**
     * @param Map srcMap - the map (object?) to travers baesd on path param
     * @param path - the string defining the path through the jsonslurped object (maps/collections/leafNodes with primative values)
     * @param separator - string separator to split path on, defaults to slash '/'
     * @param valToSet
     * @return updated Map
     *
     * steps:
     *
     * todo - refactor me!! convoluted code
     *
     * todo consider returning a new (cloned?) map, rather than the original...? refactor...
     */
    static def setJsonObjectNode(Map srcMap, String path, String separator = '/', def valToSet = '') {
        log.debug "Process path: [$path] in src:$srcMap "
        List<String> segments = path.split(separator)
        segments.remove(0)
        int numSegments = segments.size()
        def element = srcMap
        segments.eachWithIndex { String seg, int depth ->
            Integer index = null
            if (seg.isInteger()) {
                index = Integer.parseInt(seg)
                log.info "converted int-like string ($seg) to Integer: $index"
            }
            log.debug "\t\t$depth - $seg) check if it exists... "
            def child = getElement(seg, element)
            if (!child) {
                log.info "\t\tMISSING element segment ($seg) in path: $path..."
                if (index) {
                    log.warn "$depth) Creating a new COLLETION! does this make sense? create collection with $seg size, if > 1 then there wil be empty item...??! Path: $path "
                    child = new ArrayList(index + 1)
                    if (isLeafTarget(depth, numSegments)) {
                        child[index] = valToSet
                    } else {
                        String nextSegment = segments[depth + 1]
                        if (nextSegment.isInteger()) {
                            log.warn "creating child list with empty values...!!!"
                            child[index] = new ArrayList(Integer.parseInt(nextSegment) + 1)
                        } else {
                            child = [:]
                        }
                    }
                    element[index] = child
                    // todo -- stopped here for overnight break, revisit and fix bugs and any incomplete code in this entire function

                } else {
                    log.info "\t\t$depth) (Map element) create node $seg with empty map..."
                    if (isLeafTarget(depth, numSegments)) {
                        log.debug "Hit target segment, no need to create child map..."
                    } else {
                        String nextSegment = segments[depth + 1]

                        child = [:]
                        log.info "\t\t$depth) Hit (missing) target segment ($seg), create child map ..."
                        log.warn "todo -- $depth) Hit (missing) target segment ($seg), add code to handle collection rather than map..."
                    }
                    element[seg] = child
                    log.info "create missing segment: $seg -> $element --full source: $srcMap"
                }
            } else {
                log.info "\t\tFound existing segment ($seg) -> $element"
                if (isLeafTarget(depth, numSegments)) {
                    if (index) {
                        log.info "set list element ($index) value here..."
                        element[index] = valToSet
                    } else {
                        log.info "set Map element ($seg) value here..."
                        element[seg] = valToSet
                    }
                }
                log.info "?? more code here??...."
            }

            // are we at the leaf node to set?
            if (isLeafTarget(depth, numSegments)) {
                if (child instanceof Map || child instanceof Collection) {
                    log.warn "Setting val($valToSet) on a map or collection!!? ${child.getClass().getSimpleName()} -- $child"
                }
                element[seg] = valToSet
                log.debug "\t\t$depth) returning 'parent' segment ($seg) that contains updated value \"$valToSet\" for path($path)"
//                    element = child
                log.info "\t\t$depth) set final leaf node $seg (valToSet: $valToSet) on element: $element"
            } else if (depth < numSegments - 1) {
                log.debug "$depth) Continue with next segment..."
                element = child
            }

        }

        log.info " Updated element ( $path ):  $element  -- srcMap: ${srcMap}"
        return element
    }

    static boolean isLeafTarget(int depth, int numSegments) {
        depth == (numSegments - 1)
    }

/*
// placeholder for trying new approaches to getting (or setting?) a value with missing parent entries
    def getMapWithdefaultInject(Map map, String path){

    }
*/


/**
 * Helper function to get a flattened list of node paths in XMLParser Node object
 * @param node XMLParser node result of a parse call
 * @param level tracking variable to help define node depth (needed? valuable?)
 * @param separator string to use to build a concatonated string path
 * @return List of string paths
 *
 * todo are there intermediate things we care about that are not leaf nodes?
 */
    static List flattenXmlPath(Node node, int level = 0, String separator = '/') {
        String name = separator + node.name()
        def attributes = node.attributes()
        level++
        log.debug '\t'.multiply(level) + "$level) $name"
        List pathList = [name]
//        node.childNodes().each { childNode ->
        node.children().each { childNode ->
            if (childNode instanceof Node) {
                log.debug '\t\t'.multiply(level) + "$level) child dive... ${childNode.name()}"
                def childPaths = flattenXmlPath(childNode, level)
                childPaths.each {
                    String path = name + it
//                    String path = name + separator + it
                    pathList << path
                }
            } else {
                log.debug "Child Not a Node: ${childNode.class.simpleName}"
            }
        }
        return pathList
    }


/**
 * similar to flattenXmlPath, but specify a Map of thingNames (datasource, indexpipeline,...?) and regex patterns to include attribs in path (to help disambiguate
 *
 * @param node result of parsing source xml
 * @param level helper var
 * @param separator string to use in building path
 * @param attribsForPath Map of patterns to include attribtes in resulting paths to disambiguate (helpful in comparing left to right XML objects
 * @return
 */
    static List<Map<String, Object>> flattenXmlPathWithAttributes(Node node, int level = 0, String separator, Map<String, Pattern> attribsForPath) {
        String nodeName = node.name()
        def attributes = node.attributes()
        List<String> sortedKeys = attributes.keySet().sort()
        Pattern pattern = attribsForPath[nodeName]
        if (pattern) {
            log.debug "found matching pattern for node name: $nodeName: $pattern"
        } else {
            pattern = attribsForPath['']
            if (pattern) {
                log.debug "no matching pattern for node name,: $nodeName, found default (''): $pattern"
            } else {
                log.debug "no matching pattern for node name,: $nodeName, nor was there a default..."
            }
        }
        def nameKeys = sortedKeys.findAll {
            it ==~ pattern
        }
        def nameAttribs = attributes.subMap(nameKeys)
        String name
        if (nameAttribs) {
            name = separator + nodeName + "$nameAttribs"
        } else {
            name = separator + node.name()
        }
        level++
        log.debug '\t'.multiply(level) + "$level) $name"
        List pathList = [[name: name, attributes: attributes]]
        node.children().each { childNode ->
            if (childNode instanceof Node) {
                log.debug '\t\t'.multiply(level) + "$level) child dive... ${childNode.name()}"
                def childPaths = flattenXmlPathWithAttributes(childNode, level, separator, attribsForPath)
                childPaths.each { Map<String, Object> child ->
                    String path = name + child.name
                    pathList << [name: path, attributes: child.attributes]
                }
            } else {
                log.debug "Child Not a Node: ${childNode.class.simpleName}"
            }
        }
        return pathList
    }


    /**
     * Flatten object, get path with object (reference?)
     * @param nested object (list and/or map) to flatten
     * @param level helper to track depth (is this helpful?)
     * @return Map with flattened path(string) as key, and the given object as value
     */
    static Map<String, Object> flattenWithLeafObject(def object, int level = 0, String prefix = '/', String separator = '/') {
        Map<String, Object> entries = [:]
        log.debug "$level) flattenPlusObject: $object..."
        if (object instanceof Map) {
//            def keyset = object.keySet()
            Map currentDepthMap = (Map) object
            currentDepthMap.each { String key, Object value ->
                log.debug "\t" * level + "$level)Key: $key"
                if (value instanceof Map || value instanceof List) {
                    level++
                    Map<String, Object> children = flattenWithLeafObject(value, level, '/', '/')
                    children.each { String child, Object childObject ->
                        String path = separator + key +  child
                        if (childObject instanceof Map || childObject instanceof List) {
                            log.debug "skip this (${childObject.getClass().simpleName}"
                            entries[path] = null
                        } else {
                            entries[path] = childObject
                        }
                    }
                    log.debug "\t" * level + "submap keys: ${children}"
                } else {
                    log.info "\t\t$level) setting Map key($key) to value($value)"
                    entries[separator + key] = value
                    log.debug "\t" * level + "\t\tLeaf node: $key"
                }
            }
            log.debug "$level) after collect entries"

        } else if (object instanceof List) {
            log.debug "\t" * level + "$level) List! $object"
            List currentDepthList = (List) object
            currentDepthList.eachWithIndex { def val, int counter ->
                if (val instanceof Map) {
                    level++
                    def children = flattenWithLeafObject(val, level, prefix, separator)
                    children.each { String childName, Object childVal ->
                        String path = "${counter}${separator}${childName}"
//                        String path = "[${counter}].${childName}"
                        log.info "What do we do here? $path [$childVal] -- Skip??"
                        entries[path] = childVal
                    }
                    log.debug "\t" * level + "submap keys: ${children}"
                } else if (val instanceof List) {
                    String path = "${counter}${separator}${childName}"
                    log.warn "What do we do here? $path [$childVal] -- Skip??"
                    entries[path] = childVal

                } else {
                    log.info "\t" * level + "$level:$counter) LIST value not a collection, leafNode? $val"
                    String path = "/${counter}"
                    entries[path] = val
                }
            }
            log.debug "done with list"
        } else {
            log.warn "$level) other?? $object"
        }
        return entries
    }


/**
 * simple method to 'walk' Json Slurped object, and get element paths
 * @param object Json Slurped object (maps/lists)
 * @param level helper var to track depth in recursive calls
 * @return list of paths <String>s
 */
    static List<String> flatten(def object, int level = 0) {
        List<String> entries = []
        log.debug "$level) flatten object: $object..."
        if (object instanceof Map) {
            def keyset = object.keySet()
            Map map = (Map) object
            keyset.each { String key ->
                def value = map[key]
                log.debug "\t" * level + "$level)Key: $key"
                if (value instanceof Map || value instanceof List) {
                    level++
                    def children = flatten(value, level)
                    children.each { String child ->
                        entries << "${key}.${child}".toString()
                    }
                    log.debug "\t" * level + "submap keys: ${children}"
                } else {
                    entries << "$key"
                    log.debug "\t" * level + "\t\tLeaf node: $key"
                }
                log.debug "next..."
            }
            log.debug "$level) after collect entries"

        } else if (object instanceof List) {
            log.debug "\t" * level + "$level) List! $object"
            List list = (List) object
            list.eachWithIndex { def val, int counter ->
                if (val instanceof Map || val instanceof List) {
                    level++
                    def children = flatten(val, level)
                    children.each { String child ->
                        entries << "[${counter}].${child}"
                    }
                    log.debug "\t" * level + "submap keys: ${children}"
                } else {
                    log.debug "\t" * level + "$level:$counter) List value not a collection, leafNode? $val"
                    entries << "[${counter}]"
                }
            }
            log.debug "done with list"
        } else {
            log.warn "$level) other?? $object"
        }
        return entries
    }

/**
 * util function to get an (output) folder (for exporting), create if necessary
 * @param dirPath
 * @return
 */
    static File getOrMakeDirectory(String dirPath) {
        File folder = new File(dirPath)
        if (folder.exists()) {
            if (folder.isDirectory()) {
                log.debug "Folder (${folder.absolutePath} exists, which is good"
            } else {
                log.warn "Folder (${folder.absolutePath}) exists, but is not a folder, which is bad"
                throw new IllegalAccessException("Job Folder (${folder.absolutePath}) exists, but is not a folder, which is bad, aborting")
            }
        } else {
            def success = folder.mkdirs()
            if (success) {
                log.info "\t\tCreated folder: ${folder.absolutePath}"
            } else {
                log.warn "Folder (${folder.absolutePath}) could not be created, which is bad"
                throw new IllegalAccessException("Folder (${folder.absolutePath}) exists, could not be created which is bad, aborting")
            }
        }
        folder
    }

/**
 * placeholder for getting a psuedo source-control folder name for exports (and potentially imports / restore)
 * @param date
 * @param dateFormat --
 * @return a "sort friendly" datestamp with hour & minute to allow multiple snapshots per day (or per hour)...
 */
    static String getVersionName(Date date = new Date(), DateFormat dateFormat = new SimpleDateFormat('yyyy-MM-dd.hh.mm')) {
        String s = dateFormat.format(date)
    }


}

