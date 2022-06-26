package com.lucidworks.ps.misc

import org.apache.log4j.Logger
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/21/22, Tuesday
 * @description:
 */

class NodeAddTest extends Specification {
    static final Logger log = Logger.getLogger(this.class.name);
    static final String valToSet = 'was I added?'
    static final String aOneVal = 'one'
    static final String b1Val = 'three'
    static final String DEFAULT_SEPARATOR = '/'
    static final String aOnePath = '/a/one'
    static final String missingPath = '/a/one/three'

    @Shared
    Map srcMap

    void setup() {
        srcMap = [
                a             : [one: aOneVal, two: 'two'],
                b             : [b1Val, 'four'],
                componsiteList: ['comp1', 'comp2', [submapkey1: 'comp map 1 val']]
        ]

    }

// https://spockframework.org/spock/docs/1.3/all_in_one.html#_method_unrolling
    @Unroll
    def "basic get values sanity checks"() {
        when:
        def result = getObjectNodeValue(srcMap, path, DEFAULT_SEPARATOR)

        then:
        result == checkValue

        where:
        path                           | separator         | checkValue
        aOnePath                       | DEFAULT_SEPARATOR | aOneVal
        '/a/two'                       | '/'               | 'two'
        '/b/0/'                        | DEFAULT_SEPARATOR | b1Val
        '/componsiteList/2/submapkey1' | DEFAULT_SEPARATOR | 'comp map 1 val'
    }

    @Unroll
    def "basic SET values sanity checks"() {
        given:
        Map newMapToAdd = [bizz: 1, buzz: 2]

        when:
        def resultNewLeaf = setObjectNodeValue(srcMap, '/newTopLeaf', valToSet, DEFAULT_SEPARATOR)
        def resultThree = setObjectNodeValue(srcMap, '/a/three', valToSet, DEFAULT_SEPARATOR)
        def resultBizz = setObjectNodeValue(srcMap, '/a/four', newMapToAdd, DEFAULT_SEPARATOR)

        then:
        resultNewLeaf == valToSet
        srcMap.newTopLeaf == valToSet

        resultThree == valToSet
        srcMap.a.three == valToSet
        resultBizz == '{}'
        srcMap.a.four == newMapToAdd
    }

    def "basic SET values sanity checks with datatables"() {
        given:
        String vnew = 'new set val'

        when:
        def result = setObjectNodeValue(srcMap, path, v, DEFAULT_SEPARATOR)

        then:
        checkValue == result

        where:
        path                           | separator         | v           | checkValue
        '/newTopLeaf'                  | DEFAULT_SEPARATOR | "vnew"      | 'vnew'
        '/a'                           | DEFAULT_SEPARATOR | "vnew"      | 'vnew'
        '/a/two'                       | '/'               | 'new value' | 'new value'
        '/b/0/'                        | DEFAULT_SEPARATOR | 'new value' | 'new value'
        '/componsiteList/2/submapkey1' | DEFAULT_SEPARATOR | 'new value' | 'new value'
    }

/*
    def "should add a missing path node"() {
        given:
        String separator = '/'
        Map srcMap = [a: [one: 1, two: 2], b: [1, 2]]
        String missingPath = '/a/three/myMissingLeaf'
        String valToSet = 'was I added?'

        when:
        List parts = missingPath.split('/')

        then:
        parts.size() == 4
    }
*/

    /**
     * break the path into segments, walk the source object to get values,
     * try to handle maps AND collections(Lists)
     * @param srcMap
     * @param path
     * @param separator
     * @return
     */
    def getObjectNodeValue(Map srcMap, String path, String separator = DEFAULT_SEPARATOR) {
        log.info "Process path: [$path] in src:$srcMap "
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
                log.info "\t\t$depth) Found segment ($seg) -> $element"
            }
            element = child

        }
        log.info "return Path ($path) element (value): $element "
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
    def setObjectNodeValue(Map srcMap, String path, def valToSet, String separator = DEFAULT_SEPARATOR, boolean createIfMissing = true) {
        log.info "Process SET path: [$path] to value ($valToSet) in src:$srcMap -- create if missing: $createIfMissing"
        List<String> segments = parsePath(path, separator)
        int numSegments = segments.size()
        def result

        // start at the top, element will be set to each 'child' element as we walk the parsed segments
        def element = srcMap
        // loop through path segments, stop when reaching the last element, or a (parent) segment is null
        for (int depth = 0; depth < numSegments && element; depth++) {
            String currentSegment = segments[depth]
            Object child = getChildElement(currentSegment, element)
            if (isLeafTarget(depth, numSegments)) {
                result = setLeafNodeValue(depth, currentSegment, path, valToSet, element)
            } else {
                if (child) {
                    log.debug "$depth) we have a valid child ($child), and we are not at the leaf node (current segment: $currentSegment), keep iterating through path..."
                } else {
                    log.warn "$depth) found a missing 'child' for segment ($currentSegment), create it if createIfMissing($createIfMissing) is true..."
                    if (createIfMissing) {
                        child = createMissingNode(element, currentSegment)
                    }
                }
            }
//            // todo -- handle creating missing elements
//            log.debug "\t\t$depth - $currentSegment) check if it exists... "
//            if (!child) {
//                log.info "\t\t${depth}) encountered MISSING segment: $currentSegment -> return null, exit for loop..."
//            } else {
//                log.info "\t\t$depth) Found segment ($currentSegment) -> $element"
//            }
//            element = child
        }

        log.info "return Path ( $path ) with updated element ($element) set to new value ($valToSet) -- result: $result"
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
     */
    public String setLeafNodeValue(int depth, String currentSegment, String path, def valToSet, element) {
        def result
        log.info "$depth) We are at the leafNode (seg: $currentSegment) in path($path) to set the value ($valToSet) in element ($element)..."
        if (element instanceof List) {
            if (currentSegment.isInteger()) {
                log.info "Set LIST value ($valToSet) in element($element) with position: $currentSegment"
                Integer index = Integer.parseInt(currentSegment)
                List listElement = (List) element
                if (index > listElement.size() - 1) {
                    String msg = "(List) element (size:${listElement.size()}) is smaller than index, bailing with error (for now... add more code to handle this!)"
                    log.warn msg
                    throw (new IllegalArgumentException(msg))
                } else {
                    log.info "set list element (${index}) to value: $valToSet"
                    element[index] = valToSet
                    result = valToSet
                }
            } else {
                String msg = "We are in a List, but the segment ($currentSegment) is not an integer!! bailing!!!"
                log.warn msg
                throw (new IllegalArgumentException(msg))
            }

        } else if (element instanceof Map) {
            log.info "Set MAP key($currentSegment) to value ($valToSet) in parent map($element)"
            element[currentSegment] = valToSet
            // todo -- what makes sense for method return? testing approach of returning valToSet (srcMap should be updated in place)
            result = valToSet
        } else {
            String msg = "Unknown element type (${element.getClass().simpleName}), bailing!"
            log.warn msg
            throw (new IllegalArgumentException(msg))
        }
        return result
    }

    public List<String> parsePath(String path, String separator) {
        List<String> segments = path.split(separator)
        if (!segments[0]) {
            log.debug "Remove empty first segment because path ($path) starts with separator($separator)"
            segments.remove(0)
        }
        segments
    }

    static boolean isLeafTarget(int depth, int numSegments) {
        depth == (numSegments - 1)
    }


/**
 * handle processing for map or list element
 * @param currentSegment the current <String> segment in the path
 * @param currentElement
 * @return JsonSurped 'node' (Map, List, leaf node...)
 */
    def getChildElement(String currentSegment, Object currentElement) {
        def child
        if (currentSegment.isInteger()) {
            log.info "segment ($currentSegment) looks like an int, converting to integer (assuming no Int-like map keys...): $currentSegment"
            child = currentElement[Integer.parseInt(currentSegment)]
        } else {
            child = currentElement[currentSegment]
            log.debug "segment ($currentSegment) looks like an map key, got child: $child"
        }
        child
    }


/*
    def "should add a missing node via groovy map.withDefault and inject"(){
        given:
        // https://stackoverflow.com/questions/56683855/groovy-map-populate-with-default-element
        def result = [1,2,3,4].inject([:].withDefault{[]}){ m, i ->
            m[ i%2==0 ? 'odd' : 'even' ] << i
            m
        }
        // => [even:[1, 3], odd:[2, 4]]
    }
*/

    /**
     * create a missing element in the path
     * @param parentElement
     * @param missingSegment
     * @param newEmptyChild defaults to empty map, send an empty list of the next element is list rather than map
     * @return new element (empty map or list)
     */
    Object createMissingNode(Object parentElement, String missingSegment, def newEmptyChild = [:]) {
        if (parentElement instanceof List) {
            Integer index = Integer.parseInt(missingSegment)
            List parentList = (List) parentElement
            if (index > parentList.size()) {
                String msg = "Index: $index is greater than parentlist size(${parentList.size()})!!"
                log.warn msg
                throw (new IllegalArgumentException(msg))
            } else {
                parentList[index] = newEmptyChild
            }
        } else if (parentElement instanceof Map) {
            log.info "Create missing element for segment ($missingSegment) with default new node: $newEmptyChild"
            ((Map) parentElement).put(missingSegment, newEmptyChild)
        }
        return newEmptyChild
    }
}
