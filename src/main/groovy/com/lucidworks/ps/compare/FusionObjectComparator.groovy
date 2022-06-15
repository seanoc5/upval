package com.lucidworks.ps.compare

import com.lucidworks.ps.upval.Helper
import org.apache.log4j.Logger

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/23/22, Monday
 * @description: helper class to compare two different Lists of datalefts
 * @deprecated -- see BaseComparator as newer approach
 * we use the terminology left and right losely, but the aim is to help understand the difference reporting
 */
class FusionObjectComparator {
    Logger log = Logger.getLogger(this.class.name);

    String compareLabel
    List<Map> left = []
    List<Map> right = []
    CompareCollectionResults compareCollections
    Map<String, CompareObjectsResults> compareObjectsResultsMap = [:]

    FusionObjectComparator(String compareLabel, List<Map<String, Object>> left, List<Map<String, Object>> right) {
        log.info "Starting collection (type: $compareLabel) comparison with (${left.size()}) left objects, and (${right.size()}) right objections..."
        this.compareLabel = compareLabel
        this.left = left
        this.right = right
        List ignoreValueDifferences = []
        compareCollections = new CompareCollectionResults(compareLabel, left, this.right, ignoreValueDifferences)
//        compare()
    }

    FusionObjectComparator(String compareLabel, Map<String, Object> left, Map<String, Object> right) {
        log.info "Starting collection (type: $compareLabel) comparison with (${left.keySet()}) left keys, and (${right.keySet()}) right keys..."
        this.compareLabel = compareLabel
        this.left = left
        this.right = right
        List ignoreValueDifferences = []
//        compareCollections = new CompareCollectionResults(compareLabel, left, this.right, ignoreValueDifferences)
    }


    CompareCollectionResults compare() {
        Integer diffCount = compareItemCounts()

        Map diffMap = compareIds()

        compareCollections.sharedIds.each { String id ->
            log.info "Comparing shared object with id: $id"
            def leftObject = left.find {it.id == id}
            def rightObject = right.find{it.id == id}
            CompareObjectsResults objectsResults = compareObjects(leftObject, rightObject)
            compareObjectsResultsMap[id] = objectsResults
            log.debug "Compare results: $objectsResults"
        }

        return compareCollections
    }

    Integer compareItemCounts() {
        compareCollections.countDifference = left.size() - right.size()
        if(compareCollections.countDifference != 0){
            log.info "\t\t(${this.compareLabel}) Count difference: ${compareCollections.countDifference}"
        }
        return compareCollections.countDifference
    }

    Map<String, List<String>> compareIds() {
        def leftIds = left.collect { it.id }
        def rightIds = right.collect { it.id }

        compareCollections.leftOnlyIds = leftIds - rightIds
        if(compareCollections.leftOnlyIds){
            log.info "${this.compareLabel} left only ids: ${compareCollections.leftOnlyIds}"
        }
        compareCollections.rightOnlyIds = rightIds - leftIds
        if(compareCollections.rightOnlyIds){
            log.info "${this.compareLabel} d only ids: ${compareCollections.leftOnlyIds}"
        }
        compareCollections.sharedIds = leftIds.intersect(rightIds)

        compareCollections.leftOnlyItems = left.findAll { compareCollections.leftOnlyIds.contains(it.id) }
        compareCollections.rightOnlyItems = right.findAll { compareCollections.rightOnlyIds.contains(it.id) }
        Map diffMap = [leftOnly: compareCollections.leftOnlyIds, rightOnly: compareCollections.rightOnlyIds]
        return diffMap
    }

    CompareObjectsResults compareObjects(def left, def right) {
        CompareObjectsResults objectsResults = new CompareObjectsResults(compareLabel, left, right)
        def leftKeyPaths = Helper.flatten(left, 1)
        def rightKeyPaths = Helper.flatten(right, 1)

        def leftOnly = leftKeyPaths - rightKeyPaths
        def rightOnly = rightKeyPaths - leftKeyPaths
        objectsResults.leftOnlyKeys = leftOnly
        objectsResults.rightOnlyKeys = rightOnly

        def shared = leftKeyPaths.intersect(rightKeyPaths)
        objectsResults.sharedKeys = shared

        return objectsResults
    }

    String toString(){
        def differentObjects = compareObjectsResultsMap.findAll {String id, CompareObjectsResults objectsResults->
            objectsResults.isFunctionallyDifferent()
        }
        String indentA = '\t\t'
        StringBuilder sb = new StringBuilder()
        sb.append("Collection type: $compareLabel\n")
        sb.append(compareCollections.toString(indentA) + "\n")

        String indentB = '\t\t\t\t'
        differentObjects.each { String id, CompareObjectsResults objectResults ->
            sb.append("${indentB}${id}:${objectResults.toString()}\n")
        }
        return sb.toString()
    }
}


