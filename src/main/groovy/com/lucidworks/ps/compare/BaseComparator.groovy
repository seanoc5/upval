package com.lucidworks.ps.compare

import com.lucidworks.ps.model.BaseObject
import com.lucidworks.ps.transform.JsonObject
import org.apache.log4j.Logger
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/11/22, Saturday
 * @description:
 */

/** generic 'object' comparison, assumes just a hybrid collection (combination of Map/List),
 * for example a fusion json definition (datasource or pipeline, or objects.json) slurped into a Java Collection
 */
class BaseComparator {
    Logger log = Logger.getLogger(this.class.name);

    String compareLabel
    def left
    Map<String, Object> leftFlatMap
    Set<String> leftKeyPaths
    def leftOnlyKeys

    def right
    Map<String, Object> rightFlatMap
    Set<String> rightKeyPaths
    def rightOnlyKeys

    def ignoreValueDifferences
    def matchChildrenOrder


    /**
     * Basic constructor with defaults values that can be overriden
     * @param left - collection (Map or List)
     * @param right - collection (Map or List)
     * @param ignoreValueDifferences - an optional regex pattern for element names to skip comparing values (just interested in structure)
     * @param matchChildrenOrder - an optional regex pattern for element names to MATCH for comparing element order (default is order not relevant, but structure is)
     */
    BaseComparator(String compareLabel, def left, def right, def ignoreValueDifferences = null, def matchChildrenOrder = null) {
        if(left instanceof BaseObject){
            log.debug "Getting source items from BaseObject (${left.itemType})"
            this.left = left.srcItems
        } else {
            this.left = left
        }
        if(right instanceof BaseObject){
            log.debug "Getting source items from BaseObject (${right.itemType})"
            this.right = right.srcItems
        } else {
            this.right = right
        }
        this.compareLabel = compareLabel
        this.ignoreValueDifferences = ignoreValueDifferences
        this.matchChildrenOrder = matchChildrenOrder
        String msg = "Constructor with Left object(${describeObject(left)}) and right object (${describeObject(right)})"
        log.info msg
    }

    /**
     * compare the left and right objects, create a CompareObjectResults object to hold result information, populate that object
     * <br/>
     * todo -- refactor CompareObjectResults object and call here... should/could be static call? benefit of maintaining state in this object??
     * @return CompareObjectResults object containing information about the comparison results
     */
    CompareJsonObjectResults compare() {
        CompareJsonObjectResults objectsResults = new CompareJsonObjectResults(this.compareLabel, left, right)
        leftFlatMap = JsonObject.flattenWithLeafObject(left, 1, '/', '/')
        leftKeyPaths = leftFlatMap.keySet()

        rightFlatMap = JsonObject.flattenWithLeafObject(right, 1, '/', '/')
        rightKeyPaths = rightFlatMap.keySet()

        leftOnlyKeys = leftKeyPaths - rightKeyPaths
        rightOnlyKeys = rightKeyPaths - leftKeyPaths
        if (leftOnlyKeys) {
            objectsResults.leftOnlyKeys = leftOnlyKeys
            ComparisonResult diff = new ComparisonResult(this.compareLabel, ComparisonResult.DIFF_LEFT_ONLY, leftOnlyKeys.toString())
            objectsResults.differences << diff
        }
        objectsResults.rightOnlyKeys = rightOnlyKeys
        if (rightOnlyKeys) {
            objectsResults.rightOnlyKeys = rightOnlyKeys
            ComparisonResult diff = new ComparisonResult(this.compareLabel, ComparisonResult.DIFF_RIGHT_ONLY, rightOnlyKeys.toString())
            objectsResults.differences << diff
        }

        def shared = leftKeyPaths.intersect(rightKeyPaths)
        objectsResults.sharedKeys = shared
        shared.each { String sharedItemPath ->
            log.debug "Compare objects: [${sharedItemPath}] "
            def leftItem = leftFlatMap[sharedItemPath]
            def rightItem = rightFlatMap[sharedItemPath]
            boolean ignoreValues = false
            boolean matchChildOrder = false

            if (leftItem instanceof Map) {
                log.warn "\t\t\t\tSkip comparing values for shared Map item (type: ${leftItem.getClass().simpleName}), sharedPath: $sharedItemPath"
            } else {
                ignoreValues = (sharedItemPath ==~ ignoreValueDifferences)
                if(ignoreValues){
                    log.info "IGNORE value differences for this shared path: '$sharedItemPath' matches pattern: ${ignoreValueDifferences}"
                }
                ComparisonResult diff = compareValues(sharedItemPath, leftItem, rightItem, ignoreValues, matchChildOrder)
                if (diff.isDifferent()) {
                    objectsResults.differences << diff
                    log.debug "\t\tAdd DIFFERENCE: $diff"
                } else {
                    objectsResults.similarities << diff
                    log.debug "\t\tAdd similarity: $diff"
                }
            }

        }

        log.info "Comparison results: ${objectsResults.toString()}"
        return objectsResults
    }

    /**
     * Compare an 'object' non-hierarchically -- just at the given level. Look for functional equivalence
     *
     */
    ComparisonResult compareValues(String compareLabel, def left, def right, boolean ignoreValueDifferences, boolean matchChildOrder) {
        String leftClassName = left.getClass().name
        String rightClassName = right.getClass().name

        ComparisonResult diff

        if (leftClassName == rightClassName) {
            log.debug "\t\tSame class names (which is a good start..): ${left.getClass().simpleName}"
            // todo -- more logic/refactoring here to focus oon the differences...
            if (left instanceof Map) {
                log.warn "These left($left) and right ($right) are Maps ($leftClassName), we should not be comparing these here, let `BaseComparator` walk the collection and compare..."

            } else if (left instanceof List) {
                diff = compareLists(compareLabel, left, right, matchChildOrder)

            } else {
//                boolean ignoreValues = (compareLabel ==~ ignoreValueDifferences)
                // if comparison label matches ignore pattern, then don't compare values
                if (ignoreValueDifferences) {
                    log.info "[$compareLabel] ignoring values because parent method told us to ignore value differences... (object path matches ignore pattern in parent method)"
                }
                diff = compareLeafThing(compareLabel, left, right, ignoreValueDifferences)
            }
        } else {
            diff = new ComparisonResult(compareLabel, ComparisonResult.DIFF_CLASSES, "Left class name: [$leftClassName] differs from Right class name: [$rightClassName]")
        }
        log.debug "\t\t${diff}"

        return diff
    }

    /**
     * compare a leaf node (typically some base Java type like string, number,...)
     * @param compareLabel
     * @param left (expect String, Integer,...)
     * @param right (expect String, Integer,...)
     * @param ignoreValueDifferences if true,
     * @return Difference
     */
    ComparisonResult compareLeafThing(String compareLabel, Object left, Object right, boolean ignoreValueDifferences) {
        String diffType, description

        String leftClassName = left.getClass().name
        String rightClassName = right.getClass().name
        String lstr = left.toString()
        String rstr = right.toString()

        if (leftClassName.equals(rightClassName)) {
            if (lstr.equals(rstr)) {
                description = "(toString()) Values are EQUAL: left:($lstr) == right:($rstr) (object types: ${left.getClass().simpleName})"
                diffType = ComparisonResult.EQUAL
            } else if (ignoreValueDifferences) {
                description = "Ignore value differences==true, objects have same class, so are SIMILAR: left str:($lstr) and right str:($rstr)"
                diffType = ComparisonResult.SIMILAR
            } else {
                description = "Values are DIFFERENT: left:($lstr) and right:($rstr)"
                diffType = ComparisonResult.DIFF_VALUES
            }
        } else {
            diffType = ComparisonResult.DIFF_CLASSES
            description = "Left class name: [$leftClassName] differs from Right class name: [$rightClassName]"
            if (lstr.equals(rstr)) {
                String msg = "string values are equal!! LStr:(${lstr} == (${rstr})"
                log.warn "[${compareLabel}] Classes are different, $msg -- Is this common? More code here...?"
                description += " -- ${msg}"
            }
        }
        ComparisonResult diff = new ComparisonResult(compareLabel, diffType, description)
        log.debug "\t\t${diff.toString()}"
        return diff
    }

    ComparisonResult compareLists(String compareLabel, List left, List right, boolean matchChildOrder) {
        String diffType, description
        log.debug "$compareLabel) comparing lists..."
        if (matchChildOrder) {
            String lstr = left.toString()
            String rstr = right.toString()
            if (lstr.equalsIgnoreCase(rstr)) {
                description = "Values are the Similar: left:($lstr) and right:($rstr)"
                diffType = ComparisonResult.EQUAL
            } else {
                description = "Values (or order) are the Different: left:($lstr) and right:($rstr)"
                diffType = ComparisonResult.DIFF_VALUES
                log.debug "\t\t$diffType) $description"
            }
        } else {
            log.debug "\t\tCompare list without order... $left to $right"
            if (left.sort() == right.sort()) {
                diffType = ComparisonResult.EQUAL
                description = "Values are equivalent (not comparing order): $left"
            } else {
                diffType = ComparisonResult.DIFF_VALUES
                description = "Values DIFFER (not comparing order): left(${left}) != right(${right})"
            }
        }
        ComparisonResult difference = new ComparisonResult(compareLabel, diffType, description)
        return difference
    }

    String describeObject(def object){
        String desc
        if(object instanceof Map){
            Map m = (Map)object
            def keys = m.keySet()
            desc = "Map (${object.getClass().simpleName}) has (${keys.size()}) keys"
        } else if(object instanceof List) {
            List l = (List) object
            desc = "List (${object.getClass().simpleName}) has (${l.size()}) items"
        } else {
            desc = "Object (${object.getClass().simpleName}) has value: (${object.toString()})"
        }
        return desc
    }
/*
    Comparison compareMapValues(String compareLabel, Map left, Map right, Pattern ignoreValueDifferences) {
        String diffType, description
        log.warn "comparing Maps...??? Do we need this? Should be part of flatten... yes?"
        Comparison difference = new Comparison(compareLabel, diffType, description)
        return difference
    }
*/

}
