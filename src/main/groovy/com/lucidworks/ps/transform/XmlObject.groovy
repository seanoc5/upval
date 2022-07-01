package com.lucidworks.ps.transform

import org.apache.log4j.Logger

import java.util.regex.Pattern

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   7/1/22, Friday
 * @description:
 */

class XmlObject {
    static Logger log = Logger.getLogger(this.class.name)

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

}
