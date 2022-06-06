package com.lucidworks.ps.upval


import org.apache.log4j.Logger

import java.util.regex.Pattern

class Helper {
    static Logger log = Logger.getLogger(this.class.name);

    static List flattenXmlPath(Node node, int level = 0, String separator = '/') {
        String name = separator + node.name()
        def attributes = node.attributes()
        level++
        log.info '\t'.multiply(level) + "$level) $name"
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

    static Map<String, Object> flattenPlus(def object, int level = 0) {
        Map<String, Object> entries = [:]
        log.info "$level) flattenPlus object: $object..."
        if (object instanceof Map) {
            def keyset = object.keySet()
            Map map = (Map) object
            keyset.each { String key ->
                def value = map[key]
                log.debug "\t" * level + "$level)Key: $key"
                if (value instanceof Map || value instanceof List) {
                    level++
                    Map<String, Object> children = flattenPlus(value, level)
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
                    def children = flattenPlus(val, level)
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


    static def flatten(def object, int level = 0) {
        List entries = []
        log.info "$level) flatten object: $object..."
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
                        entries << key + "." + child
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
}

