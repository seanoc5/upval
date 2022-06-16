package com.lucidworks.ps.upval


import org.apache.log4j.Logger

import java.util.regex.Pattern

/**
 * General helper class
 * todo consider refactoring flattening operations to a more obvious classname (low-priority as it is a low-level op, not big picture processing)
 */
class Helper {
    static Logger log = Logger.getLogger(this.class.name);

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

    static List<Map<String, Object>> flattenXmlPathWithAttributes(Node node, int level = 0, String separator, Map<String, Pattern> attribsForPath) {
        String nodeName = node.name()
        def attributes = node.attributes()
        List<String> sortedKeys = attributes.keySet().sort()
        Pattern pattern = attribsForPath[nodeName]
        if (pattern) {
            log.debug "found matching pattern for node name: $nodeName: $pattern"
        } else {
            pattern = attribsForPath['']
            if(pattern) {
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
        if(nameAttribs) {
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
     * experimenting with flattening a thing (java collection?) and returning path PLUS object metainfo (class name & level)
     * todo change generic in return to string,string...?
     *
     * @param object
     * @param level
     * @return  flattened path(string) plus object name (string)
     */
    static Map<String, Object> flattenPlusMeta(def object, int level = 0) {
        Map<String, Object> entries = [:]
        log.debug "$level) flattenPlus meta-info: $object..."
        if (object instanceof Map) {
            def keyset = object.keySet()
            Map map = (Map) object
            keyset.each { String key ->
                def value = map[key]
                log.debug "\t" * level + "$level)Key: $key"
                if (value instanceof Map || value instanceof List) {
                    level++
                    Map<String, Object> children = flattenPlusMeta(value, level)
                    children.each { String child, Object childObject ->
                        String path = key + "." + child
                        Map m = [level: level, objectType: childObject.getClass().name]
                        entries[path] = m
                    }
                    log.debug "\t" * level + "submap keys: ${children}"
                } else {
                    entries[key] = [level: level, objectType: object.getClass().name]
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
                    Map<String, Object> children = flattenPlusMeta(val, level)
                    children.each { String child ->
                        String path = "[${counter}].${child}"
                        entries[path] = [level: level, objectType: object.getClass().name]
                    }
                    log.debug "\t" * level + "submap keys: ${children}"
                } else {
                    log.debug "\t" * level + "$level:$counter) List value not a collection, leafNode? $val"
                    String path = counter
                    entries[path] = [level: level, objectType: object.getClass().name]
                }
            }
            log.debug "done with list"
        } else {
            log.warn "$level) other?? $object"
        }
        return entries
    }

    /**
     * Flatten object, get path plus object (reference?)
     * @param nested object (list and/or map) to flatten
     * @param level helper to track depth (is this helpful?)
     * @return Map with flattened path(string) as key, and the given object as value
     */
    static Map<String, Object> flattenPlusObject(def object, int level = 0) {
        Map<String, Object> entries = [:]
        log.debug "$level) flattenPlusObject: $object..."
        if (object instanceof Map) {
//            def keyset = object.keySet()
            Map map = (Map) object
            map.each { String key, Object value ->
                log.debug "\t" * level + "$level)Key: $key"
                if (value instanceof Map || value instanceof List) {
                    level++
                    Map<String, Object> children = flattenPlusObject(value, level)
                    children.each { String child, Object childObject ->
                        String path = key + "." + child
                        if(childObject instanceof Map || childObject instanceof List){
                            log.debug "skip this (${childObject.getClass().simpleName}"
                            entries[path] = null
                        } else {
                            entries[path] = childObject
                        }
                    }
                    log.debug "\t" * level + "submap keys: ${children}"
                } else {
                    entries[key] = value
                    log.debug "\t" * level + "\t\tLeaf node: $key"
                }
            }
            log.debug "$level) after collect entries"

        } else if (object instanceof List) {
            log.debug "\t" * level + "$level) List! $object"
            List list = (List) object
            list.eachWithIndex { def val, int counter ->
                if (val instanceof Map) {
                    level++
                    def children = flattenPlusObject(val, level)
                    children.each { String childName, Object childVal ->
                        String path = "[${counter}].${childName}"
                        log.info "What do we do here? $path [$childVal] -- Skip??"
                        entries[path] = childVal
                    }
                    log.debug "\t" * level + "submap keys: ${children}"
                } else if(val instanceof List){
                    String path = "[${counter}].${childName}"
                    log.info "What do we do here? $path [$childVal] -- Skip??"
                    entries[path] = childVal

                } else {
                    log.debug "\t" * level + "$level:$counter) List value not a collection, leafNode? $val"
                    String path = "[${counter}]"
                    entries[path] = val
                }
            }
            log.debug "done with list"
        } else {
            log.warn "$level) other?? $object"
        }
        return entries
    }


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
}

