package com.lucidworks.ps.transform

import org.apache.commons.text.StringEscapeUtils

import org.apache.log4j.Logger

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/26/22, Sunday
 * @description:
 */

/**
 * Wrapper-helper object for navigating and manipulating JsonSlurped objects.
 * <p>These are typically Maps and Lists with leaf-nodes having primative values
 * <p>Note: this differs from XMLObject; JsonObjects have hierarchy and then values at leaf-nodes, XMLObjects have attributes in the hierarchy, which means a different set of assumptions.
 * GPath works well for XML parsed objects, because the paths can/should allow access to attributes "along the way" in the hierarchy
 */
class JsonObject {
    static Logger log = Logger.getLogger(this.class.name);
    static final String DEFAULT_SEPARATOR = '/'

    /**
     * new approach to setting values, building missing nodes as needed
     * @param objectToUpdate JsonSlurped object to update at the leaf of flatpath param
     * @param flatPath flattened path of leafnode in objectToUpdate (will create as needed)
     * @param value (assuming primative, but possible sub-Map/List?
     * return result...?
     */
    def setPathValue(def objectToUpdate, String flatPath, def value, String separator = '/') {
        List<String> parts = flatPath.split(separator)
        parts.each {
            def item = 1

        }
    }

    def getChild(def current, String childPath) {
        def child
        if (current instanceof Map) {
            if (childPath.isInteger()) {
                log.warn "Current Item($current) is a map, but child path($childPath) is an integer, treating this as a map key, but is this valid??"
            }

            child = current[childPath]
            if (!child) {

            }
        }
    }


    /**
     * break the path into segments, walk the source object to get values,
     * try to handle maps AND collections(Lists)
     * @param srcMap
     * @param path
     * @param separator
     * @return
     */
    static def getObjectNodeValue(Map srcMap, String path, String separator = DEFAULT_SEPARATOR) {
        log.debug "Process path: [$path] in src:$srcMap "
        List<String> segments = path.split(separator)
        if (!segments[0]) {
            segments.remove(0)
        }
        int numSegments = segments.size()
        def element = srcMap
        // loop through path segments, stop when reaching the last element, or a (parent) segment is null
        for (int depth = 0; depth < numSegments && element; depth++) {
            String seg = segments[depth]
            Object child = getChildElement(seg, element)
            log.debug "\t\t$depth - $seg) check if it exists... "
            if (!child) {
                log.info "\t\t${depth}) encountered MISSING segment: $seg -> return null, exit for loop..."
            } else {
                log.debug "\t\t$depth) Found segment ($seg) -> $element"
            }
            element = child

        }
        log.debug "return Path ($path) element (value): $element "
        return element
    }


    /**
     * walk path segments and set the leaf value
     * if createIfMissing is true (default) will create element and any missing parents where necessary)
     * <p> the challenge comes in creating missing path segments, and setting the value in an List (especially a missing 'parent' list)
     * @param srcMap
     * @param path
     * @param valToSet
     * @param separator
     * @param createIfMissing
     * @return
     *
     * todo -- add syntax (parsing) to indicate "inserting" value into an array
     */
    static Map<String, Object> setObjectNodeValue(Map srcMap, String path, def valToSet, String separator = DEFAULT_SEPARATOR, boolean createIfMissing = true) {
        log.debug "Process SET path: [$path] to value ($valToSet) in src:$srcMap -- create if missing: $createIfMissing"
        List<String> segments = parsePath(path, separator)
        int numSegments = segments.size()
        Map<String, Object> result

        // start at the top, element will be set to each 'child' element as we walk the parsed segments
        def element = srcMap
        // loop through path segments, stop when reaching the last element, or a (parent) segment is null
        for (int depth = 0; depth < numSegments && element != null; depth++) {
            String currentSegment = segments[depth]
            log.debug "\t\t$depth) process segment: $currentSegment in element:($element)..."
            Object child = getChildElement(currentSegment, element)
            if (child) {
                // child exists, if leaf node set the element in parent, otherwise just set the parent element's child value
                log.debug "\t\t\t\t$depth) we have a valid child ($child), and we are not at the leaf node (current segment: $currentSegment), keep iterating through path..."
//                element = child
                log.debug "shifted current element to child ($child) element we found"
                // if child existed (above) check if we are currently at the leaf node, if so: set the value
                if (isLeafTarget(depth, numSegments)) {
                    log.debug "\t\t$depth) found leaf node currentSegment:$currentSegment in path: $path, set to value: $valToSet"
                    def r = setLeafNodeValue(depth, currentSegment, path, valToSet, element)
                    result = ["${path}": r]
                    element = null      // redundant? should fail on for loop condition: depth < numSegments
                } else {
                    log.debug "\t\t$depth) still navigating path, currentSegment:($currentSegment) next segment: (${segments[depth + 1]})"
                    element = child
                }

            } else {
                if (isLeafTarget(depth, numSegments)) {
                    log.info "\t\tdepth:$depth) path: $path found  currentSegment:$currentSegment  SET VALUE: $valToSet"
                    def r = setLeafNodeValue(depth, currentSegment, path, valToSet, element)
                    result = ["${path}": r]

                } else {
                    log.debug "\t\tsegment depth $depth) found a missing 'child' for segment ($currentSegment), create it if createIfMissing($createIfMissing) is true..."
                    if (createIfMissing) {
                        String nextSegment = segments[depth + 1]
                        def newEmptyChild = [:]
                        if (nextSegment.isInteger()) {
                            newEmptyChild = []
                        }
                        child = createMissingNode(element, currentSegment, newEmptyChild)
//                        result = child      // remove me? should just be element = child??
                        element = child
                    } else {
                        log.warn "\t\t param: $createIfMissing is false, so we are leaving missing element ($currentSegment) as empty/null.."
                    }
                }
            }
        }

        log.debug "return Path ( $path ) with updated element ($element) set to new value ($valToSet) -- result: $result"
        return result
    }


    /**
     * set the value of the given (JsonSlurped) 'node' -- this should handle differentiating list and/or map "parents"
     * @param depth
     * @param currentSegment
     * @param path the gpath-like path to dive into a JsonSlurped object (just maps/lists??)
     * @param valToSet intended for string/number, but perhaps also allows for more complex values to set???
     * @param element
     * @return the same valToSet param (as a success check), or null if failed???
     *
     * todo -- fix missing hierarchies -- currently returning each created segment flatly -- not correct
     */
    static String setLeafNodeValue(int depth, String currentSegment, String path, def valToSet, element) {
        def result
        log.debug "$depth) We are at the leafNode (seg: $currentSegment) in path($path) to set the value ($valToSet) in element ($element)..."
        if (element instanceof List) {
            if (currentSegment.isInteger()) {
                log.debug "Set LIST value ($valToSet) in element($element) with position: $currentSegment"
                Integer index = Integer.parseInt(currentSegment)
                List listElement = (List) element
                if (index > listElement.size() - 1) {
//                    String msg = "(List) element (size:${listElement.size()}) is smaller than index, bailing with error (for now... add more code to handle this!)"
                    String msg = "(List) element (size:${listElement.size()}) is smaller than index"
                    log.warn msg
//                    throw (new IllegalArgumentException(msg))
//                } else {
                }
                log.debug "set list element (${index}) to value: $valToSet"
                element[index] = valToSet
                result = valToSet
//                }
            } else {

                String msg = "We are in a List, but the segment ($currentSegment) is not an integer!! bailing!!!"
                log.warn msg
                throw (new IllegalArgumentException(msg))
            }

        } else if (element instanceof Map) {
            log.info "\t\t\t\tSet leafNode MAP key($currentSegment) to value ($valToSet) in parent map($element)"
            element[currentSegment] = valToSet
            // todo -- what makes sense for method return? testing approach of returning valToSet (srcMap should be updated in place)
            result = valToSet
        } else if (element instanceof String || element instanceof Integer) {
            element[currentSegment] = valToSet

        } else {
            String msg = "Unknown element type (${element.getClass().simpleName}), bailing!"
            log.warn msg
//            throw (new IllegalArgumentException(msg))
            element[currentSegment] = valToSet
        }
        return result
    }


    /**
     * simpple (override-able) method to parse the path (focused on slashy paths, but perhaps usable for dot notation??
     * @param path
     * @param separator
     * @return List of string segments to navigate (iterate through)
     */
    static List<String> parsePath(String path, String separator) {
        List<String> segments = path.split(separator)
        if (!segments[0]) {
            // todo -- decide on how to handle leading slash/separator...
            log.debug "Leading separator creates a blank for segment. This could be ok, as long as we are consistent: path:$path -> segments:$segments"
//            log.warn "Remove empty first segment because path ($path) starts with separator($separator)"
//            segments.remove(0)
        }
        segments
    }


    /**
     * simpple (override-able) method to check if we are at the 'goal' leaf node of the path, and just need to set the value
     * @param depth
     * @param numSegments
     * @return boolean (leaf node or no?)
     */
    static boolean isLeafTarget(int depth, int numSegments) {
        depth == (numSegments - 1)
    }


    /**
     * handle processing for map or list element
     * @param currentSegment the current <String> segment in the path
     * @param currentElement
     * @return JsonSurped 'node' (Map, List, leaf node...)
     */
    static def getChildElement(String currentSegment, Object currentElement) {
        def child
        if (currentSegment.isInteger()) {
            log.debug "segment ($currentSegment) looks like an int, converting to integer (assuming no Int-like map keys...): $currentSegment"
            child = currentElement[Integer.parseInt(currentSegment)]
        } else {
            child = currentElement[currentSegment]
            log.debug "segment ($currentSegment) looks like an map key, got child: $child"
        }
        child
    }


    /**
     * create a missing element in the path
     * @param parentElement
     * @param missingSegment
     * @param newEmptyChild defaults to empty map, send an empty list of the next element is list rather than map
     * @return new element (empty map or list)
     */
    static def createMissingNode(Object parentElement, String missingSegment, def newEmptyChild = [:]) {
        if (parentElement instanceof Collection) {
            if (missingSegment.isInteger()) {
                Integer index = Integer.parseInt(missingSegment)
//            List newList = new ArrayList(index)
//            newList[index] = newEmptyChild
                parentElement[index] = newEmptyChild
                log.info "\t\t\t\tset newEmptyChild to LIST for missing (Integer: $index) segment/index:($missingSegment) to parent 'element':($parentElement) with default empty child ($newEmptyChild)"
            } else {
                String msg = "invalid! parentelement($parentElement) is a collection, but missingSegment is not an integer!!!"
                log.warn msg
                throw new IllegalAccessException(msg)
            }
        } else {
//            log.debug "Leaving newEmptyChild as arg: $newEmptyChild"
            ((Map) parentElement).put(missingSegment, newEmptyChild)
            log.info "\t\tadd missing MAP entry for path segment($missingSegment) to parent 'element':($parentElement) with default empty child ($newEmptyChild)"

        }
        return newEmptyChild
    }


    /**
     * simple helper/wrapper to return 'human readable' version of escaped javascript
     * <br>Note: json does not like newlines in text, so it must be escaped (newlines become '\n', along with other items
     * @return human readable form ( real newlines and tabs)
     */
    static String unEscapeSource(String source) {
        StringEscapeUtils.unescapeEcmaScript(source)
    }

    /**
     * making this static, as we assume this Javascript object will deal with reading escaped source from json format,
     * this method
     * @param src
     * @return escaped string (ready for stuffing into a json output
     * <br>NOTE:
     */
    static String escapeSource(String src) {
        StringEscapeUtils.escapeEcmaScript(src)
    }

    /**
     * Flatten object, get path with object (reference?)
     * @param nested object (list and/or map) to flatten
     * @param level helper to track depth (is this helpful?)
     * @return Map with flattened path(string) as key, and the given object as value
     */
    static Map<String, Object> flattenWithLeafObject(def object, int level = 0, String prefix = DEFAULT_SEPARATOR, String separator = DEFAULT_SEPARATOR) {
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
                        String path = separator + key + child
                        if (childObject instanceof Map || childObject instanceof List) {
                            log.debug "skip this (${childObject.getClass().simpleName}"
                            entries[path] = null
                        } else {
                            entries[path] = childObject
                        }
                    }
                    log.debug "\t" * level + "submap keys: ${children}"
                } else {
                    entries[separator + key] = value
                    log.debug "\t\t$level) Leaf-node?? setting Map key($key) to value($value)"
//                    log.debug "\t" * level + "\t\tLeaf node: $key"
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
                        String path = "${separator}${counter}${childName}"
                        log.debug "\t\tprocess child Path:($path) -- ChildVal:[$childVal] -- Skip??"
                        entries[path] = childVal
                    }
                    log.debug "\t" * level + "submap keys: ${children}"
                } else if (val instanceof List) {
                    String path = "${counter}${separator}${childName}"
                    log.warn "What do we do here? $path [$childVal] -- Skip??"
                    entries[path] = childVal

                } else {
                    log.debug "\t" * level + "$level:$counter) LIST value not a collection, leafNode? $val"
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
     *
     * @deprecated use flattenWithLeafObject instead
     */
    static List<String> flatten(def object, int level = 0, String separator = '/') {
        log.warn "Use flattenWithLeafObject in place of this outdated 'flatten' call..."
        List<String> entries = []
        log.debug "$level) flatten object: $object... with separator:$separator"
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
                        entries << "${key}${separator}${child}".toString()
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
                        entries << "[${counter}]${separator}${child}"
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
     * code to delete map entry or list item (based on provided path/object structure
     * @param path full path to remove
     * todo - consider (regex?) pattern matching here? -- or leave that to the parent code, and just do single item removals here...?
     */
    static def removeItem(String path, Map fromMap, String separator = DEFAULT_SEPARATOR) {
        List<String> segments = parsePath(path, separator)
        def itemToRemove = null
        String lastSeg = segments[-1]
        String parentPath = getParentPath(path)
        def parentObject = getObjectNodeValue(fromMap, parentPath, separator)
//        def parent = getParentItem(path, fromMap)
        if (parentObject instanceof Collection) {
            if (lastSeg.isInteger()) {
                List list = (List) parentObject
                Integer idx = Integer.parseInt(lastSeg)
                int size = list.size()
                if (idx < size) {
                    log.warn "\tBe careful removing list items by index ($lastSeg)... doing so likely to change the list, and bork any other removals based on index (i.e. no promise we process in descending order)"
                    itemToRemove = list.get(idx)
                    list.remove(idx)
                    int newSize = list.size()
                    log.info "List had ($size) items, removed item with index($idx): $itemToRemove -- new size: $newSize"
                } else {
                    log.warn "Last segment seems to be an array index, and it is not < array size (array out of bounds situation), this may be from a previous remoteItem in the same list which borked the index of the Collection "
                }
            } else {
                log.warn "We have a list as the parent($parentObject), and last segment ($lastSeg) is not an integer!!! cannot remove!!!"
            }
        } else if (parentObject instanceof Map) {
            Map map = (Map) parentObject
            itemToRemove = map.get(lastSeg)         // todo -- minor refactoring, duplicate/confusing use of itemToRemove...
            if (itemToRemove) {
                log.debug "\t\tremove map entry($lastSeg) with item ($itemToRemove) from parent map: $map"
                itemToRemove = map.remove(lastSeg)
                log.debug "\t\tremoved $itemToRemove"
            } else {
                log.warn "Could not get element ($lastSeg) from map with keyset: ${map.keySet()}!!! Nothing to remove...?"
            }
        } else {
            log.warn "Unknown parent ($parentObject) -- not collection nor map...? Cannot remove path: $path"
        }
        return ["$path":itemToRemove]
    }

    static String getParentPath(String path, String separator = DEFAULT_SEPARATOR) {
        List<String> segments = parsePath(path, separator)
        segments.removeLast()
        String parentPath = segments.join(separator)
    }

    static def getParentItem(String path, Map flatPaths, String separator = DEFAULT_SEPARATOR) {
        String parentPath = getParentPath(path, separator)
        def parent = flatPaths[parentPath]
    }

}

