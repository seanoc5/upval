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

    def left
    def right
    Pattern ignoreValueDifferences
    Pattern matchChildrenOrder


    /**
     * Basic constructor with defaults values that can be overriden
     * @param left - collection (Map or List)
     * @param right - collection (Map or List)
     * @param ignoreValueDifferences - an optional regex pattern for element names to skip comparing values (just interested in structure)
     * @param matchChildrenOrder - an optional regex pattern for element names to MATCH for comparing element order (default is order not relevant, but structure is)
     */
    BaseComparator(def left, def right, Pattern ignoreValueDifferences = null, Pattern matchChildrenOrder = null) {
        this.left = left
        this.right = right
        this.ignoreValueDifferences = ignoreValueDifferences
        this.matchChildrenOrder = matchChildrenOrder
    }

    CompareObjectsResults compare(String label) {
        CompareObjectsResults objectsResults = new CompareObjectsResults(label, left, right)
        def leftKeyPaths = Helper.flatten(left, 1)
        def rightKeyPaths = Helper.flatten(right, 1)

        def leftOnly = leftKeyPaths - rightKeyPaths
        def rightOnly = rightKeyPaths - leftKeyPaths
        if (leftOnly) {
            objectsResults.leftOnlyKeys = leftOnly
            Difference diff = new Difference(label, Difference.DIFF_LEFT_ONLY, "The LEFT object had these items which the right did not (structural diff): $leftOnly")
        }
        objectsResults.rightOnlyKeys = rightOnly
        if (rightOnly) {
            objectsResults.rightOnlyKeys = rightOnly
            Difference diff = new Difference(label, Difference.DIFF_RIGHT_ONLY, "The RIGHT object had these items which the left did not (structural diff): $rightOnly")
        }

        def shared = leftKeyPaths.intersect(rightKeyPaths)
        objectsResults.sharedKeys = shared
        shared.each{String sharedItemPath ->
            def leftItem = left
            log.info "Compare shared item: $sharedItemPath"
        }

        return objectsResults

    }
}
