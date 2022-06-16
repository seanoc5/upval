package com.lucidworks.ps.compare

import com.lucidworks.ps.upval.Helper
import org.apache.log4j.Logger

import java.util.regex.Pattern

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

    Pattern ignoreValueDifferences
    Pattern matchChildrenOrder


    /**
     * Basic constructor with defaults values that can be overriden
     * @param left - collection (Map or List)
     * @param right - collection (Map or List)
     * @param ignoreValueDifferences - an optional regex pattern for element names to skip comparing values (just interested in structure)
     * @param matchChildrenOrder - an optional regex pattern for element names to MATCH for comparing element order (default is order not relevant, but structure is)
     */
    BaseComparator(String compareLabel, def left, def right, Pattern ignoreValueDifferences = null, Pattern matchChildrenOrder = null) {
        this.left = left
        this.right = right
        this.compareLabel = compareLabel
        this.ignoreValueDifferences = ignoreValueDifferences
        this.matchChildrenOrder = matchChildrenOrder
        String msg = "Constructor with Left object(${describeObject(left)}) and right object (${describeObject(right)})"
        log.info msg
    }

    CompareObjectResults compare(String label = null) {
        if(label){
            // todo -- fixme, muddled constructor and compare() call, pick either constructor does compare, or compare() called explicitly with label.... this is a short-term hack...
            this.compareLabel = label
        }
        CompareObjectResults objectsResults = new CompareObjectResults(this.compareLabel, left, right)
        leftFlatMap = Helper.flattenPlusObject(left, 1)
        leftKeyPaths = leftFlatMap.keySet()

        rightFlatMap = Helper.flattenPlusObject(right, 1)
        rightKeyPaths = rightFlatMap.keySet()

        leftOnlyKeys = leftKeyPaths - rightKeyPaths
        rightOnlyKeys = rightKeyPaths - leftKeyPaths
        if (leftOnlyKeys) {
            objectsResults.leftOnlyKeys = leftOnlyKeys
            Comparison diff = new Comparison(this.compareLabel, Comparison.DIFF_LEFT_ONLY, leftOnlyKeys.toString())
//            Comparison diff = new Comparison(label, Comparison.DIFF_LEFT_ONLY, "The LEFT object had these items which the right did not (structural diff): $leftOnlyKeys")
            objectsResults.differences << diff
        }
        objectsResults.rightOnlyKeys = rightOnlyKeys
        if (rightOnlyKeys) {
            objectsResults.rightOnlyKeys = rightOnlyKeys
            Comparison diff = new Comparison(this.compareLabel, Comparison.DIFF_RIGHT_ONLY, rightOnlyKeys.toString())
//            Comparison diff = new Comparison(label, Comparison.DIFF_RIGHT_ONLY, "The RIGHT object had these items which the left did not (structural diff): $rightOnlyKeys")
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
                log.debug "\t\t\t\tSkip comparing values for shared Map item (type: ${leftItem.getClass().simpleName}), sharedPath: $sharedItemPath"
            } else {
                ignoreValues = (sharedItemPath ==~ ignoreValueDifferences)
                if(ignoreValues){
                    log.info "IGNORE value differences for this shared path: $sharedItemPath"
                }
                Comparison diff = compareValues(sharedItemPath, leftItem, rightItem, ignoreValues, matchChildOrder)
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
    Comparison compareValues(String compareLabel, def left, def right, boolean ignoreValueDifferences, boolean matchChildOrder) {
        String leftClassName = left.getClass().name
        if (!leftClassName) {
            log.warn "Special class (lazy map???) more code here"
        }

        String rightClassName = right.getClass().name
        if (!rightClassName) {
            log.warn "Special class (lazy map???) more code here"
        }

        Comparison diff

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
                    log.debug "[$compareLabel] ignoring values because label matches ignore pattern '$ignoreValueDifferences'"
                }
                diff = compareLeafThing(compareLabel, left, right, ignoreValueDifferences)
            }
        } else {
            diff = new Comparison(compareLabel, Comparison.DIFF_CLASSES, "Left class name: [$leftClassName] differs from Right class name: [$rightClassName]")
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
    Comparison compareLeafThing(String compareLabel, Object left, Object right, boolean ignoreValueDifferences) {
        String diffType, description

        String leftClassName = left.getClass().name
        String rightClassName = right.getClass().name
        String lstr = left.toString()
        String rstr = right.toString()

        if (leftClassName.equals(rightClassName)) {
            if (lstr.equals(rstr)) {
                description = "(toString()) Values are EQUAL: left:($lstr) == right:($rstr)"
                diffType = Comparison.EQUAL
            } else if (ignoreValueDifferences) {
                description = "Ignore value differences==true, objects have same class, so are SIMILAR: left class:($leftClassName) and right class:($rightClassName)"
                diffType = Comparison.SIMILAR
            } else {
                description = "Values are DIFFERENT: left:($lstr) and right:($rstr)"
                diffType = Comparison.DIFF_VALUES
            }
        } else {
            diffType = Comparison.DIFF_CLASSES
            description = "Left class name: [$leftClassName] differs from Right class name: [$rightClassName]"
            if (lstr.equals(rstr)) {
                String msg = "string values are equal!! LStr:(${lstr} == (${rstr})"
                log.warn "[${compareLabel}] Classes are different, $msg -- Is this common? More code here...?"
                description += " -- ${msg}"
            }
        }
        Comparison diff = new Comparison(compareLabel, diffType, description)
        log.debug "\t\t${diff.toString()}"
        return diff
    }

    Comparison compareLists(String compareLabel, List left, List right, boolean matchChildOrder) {
        String diffType, description
        log.debug "$compareLabel) comparing lists..."
        if (matchChildOrder) {
            String lstr = left.toString()
            String rstr = right.toString()
            if (lstr.equalsIgnoreCase(rstr)) {
                description = "Values are the Similar: left:($lstr) and right:($rstr)"
                diffType = Comparison.EQUAL
            } else {
                description = "Values (or order) are the Different: left:($lstr) and right:($rstr)"
                diffType = Comparison.DIFF_VALUES
                log.debug "\t\t$diffType) $description"
            }
        } else {
            log.debug "\t\tCompare list without order... $left to $right"
            if (left.sort() == right.sort()) {
                diffType = Comparison.EQUAL
                description = "Values are equivalent (not comparing order): $left"
            } else {
                diffType = Comparison.DIFF_VALUES
                description = "Values DIFFER (not comparing order): left(${left}) != right(${right})"
            }
        }
        Comparison difference = new Comparison(compareLabel, diffType, description)
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
            desc = "Object (${object.getClass().simpleName}) has value: (${object.toString})"
        }
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
