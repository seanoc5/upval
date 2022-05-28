package com.lucidworks.ps.upval

import org.apache.log4j.Logger

class Helper {
    static Logger log = Logger.getLogger(this.class.name);

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
                    entries[key] = [level:level, objectType: object.getClass().name]
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

