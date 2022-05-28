package com.lucidworks.ps.compare

import com.lucidworks.ps.fusion.Application
import com.lucidworks.ps.upval.Helper
import org.apache.log4j.Logger

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/23/22, Monday
 * @description: helper class to compare two different Fusion Applications (or parts of)
 */

class FusionApplicationComparator {
    Logger log = Logger.getLogger(this.class.name);

    Application leftApp
    Application rightApp

    Map<String, CompareCollectionResults> collectionComparisons = [:]
    Map<String, CompareObjectsResults> objectComparisons = [:]

    public static final List<String> DEFAULTTHINGSTOCOMPARE = "configsets collections dataSources indexPipelines queryPipelines parsers blobs appkitApps features objectGroups links sparkJobs".split(' ')

    FusionApplicationComparator(Application left, Application right, def thingsToCompare = '') {
        if (!thingsToCompare) {
            thingsToCompare = this.DEFAULTTHINGSTOCOMPARE
            log.info "Using default list of things to compare: $thingsToCompare"
        } else {
            log.info "Using list of things to compare from param: $thingsToCompare"
        }
        log.info "Comparing [left] App: (${left}) ==with== (${right}) app..."
        this.leftApp = left
        this.rightApp = right
        List valueDiffsToIgnore = []
        compare(thingsToCompare, valueDiffsToIgnore)
    }

    /**
     * iterate through different things in both left and right app, compare the groups of things, and then also compare each 'shared' thing for differences
     * @param thingsToCompare
     * @return
     */
    Map<String, CompareCollectionResults> compare(def thingsToCompare, List valueDiffsToIgnore) {
        log.info "Compare things matching: (${thingsToCompare}) -- Value Diffs to ignore: $valueDiffsToIgnore"

        thingsToCompare.each { String thingType ->
            log.info "Compare things of type: $thingType"
            Object leftThings = leftApp.getThings(thingType)
            Object rightThings = rightApp.getThings(thingType)
            LeftRightCollectionResults collectionResults = new LeftRightCollectionResults(thingType, valueDiffsToIgnore)

            if (leftThings && rightThings) {
                if (leftThings?.class?.name == rightThings?.class?.name) {

                    if (leftThings instanceof List) {
                        compareIds(leftThings, rightThings, collectionResults)
                        this.collectionComparisons[thingType] = collectionResults

                        collectionResults.sharedIds.each { String id ->
                            log.info "\t\tComparing shared object with id: $id"
                            def srcObject = leftThings.find { it.id == id }
                            def destObject = rightThings.find { it.id == id }

                            CompareObjectsResults objectsResults = compareObjects(srcObject, destObject, thingType)
                            collectionResults.objectsResults[id] = objectsResults
                            log.debug "Compare results: $objectsResults"
                        }
                    } else if (leftThings instanceof Map) {
                        log.warn "TODO:: Process map (Left:${leftThings.keySet().size()}) for thing type: $thingType"
                    } else {
                        log.warn "No a list? Is this features?..."
                    }
                } else {
                    String msg = "Left thing type (${leftThings.class.name}) different than right things type (${rightThings.class.name})"
                    throw new IllegalArgumentException(msg)
                }
            } else {
                log.warn "${thingType}) leftthing($leftThings) and/or right things($rightThings) not valid..?"
            }
        }

        return collectionComparisons
    }


    LeftRightCollectionResults compareIds(List left, List right, LeftRightCollectionResults results) {
        def srcIds = left.collect { it.id }
        def destIds = right.collect { it.id }
        results.sharedIds = srcIds.intersect(destIds)
        results.leftOnlyIds = srcIds - destIds
        if (results.leftOnlyIds) {
            log.warn "Left only ids: ${results.leftOnlyIds}"
        }
        results.rightOnlyIds = destIds - srcIds
        if (results.rightOnlyIds) {
            log.warn "Right only ids: ${results.rightOnlyIds}"
        }

        results.leftOnlyItems = left.findAll {
            results.leftOnlyIds.contains(it.id)
        }
        results.rightOnlyItems = right.findAll { results.rightOnlyIds.contains(it.id) }
        return results
    }

    CompareObjectsResults compareObjects(def src, def dest, String collectionType) {
        CompareObjectsResults objectsResults = new CompareObjectsResults(collectionType, src, dest)
        def leftKeyPaths = Helper.flatten(src, 1)
        def rightKeyPaths = Helper.flatten(dest, 1)

        def leftOnly = leftKeyPaths - rightKeyPaths
        if (leftOnly) {
            log.warn "Left only ids: ${leftOnly}"
        }
        def rightOnly = rightKeyPaths - leftKeyPaths
        if (rightOnly) {
            log.warn "Right only ids: ${rightOnly}"
        }
        objectsResults.leftOnlyKeys = leftOnly
        objectsResults.rightOnlyKeys = rightOnly

        def shared = leftKeyPaths.intersect(rightKeyPaths)
        objectsResults.sharedKeys = shared
        shared.each {
            log.debug "Compare values: $it (more code here....)"
            def valueComparisons = compareValues(it, it)
        }

        return objectsResults
    }

    def compareValues(def left, def right) {
        //todo -- add logic to evaluate values of shared keys
        if (left == right) {
            log.debug "Values are the same: left:($left) and right:($right)"
        } else {
            log.info "Values are the Different: left:($left) and right:($right)"
        }
        log.debug "\t\tMORE CODE here: compareValues..."
    }

    String toString() {
        def differentObjects = objectComparisons.findAll { String id, CompareObjectsResults objectsResults ->
            objectsResults.isFunctionallyDifferent()
        }
        String indentA = '\t\t'
        StringBuilder sb = new StringBuilder()
        sb.append("Collection type: $collectionType\n")
        sb.append(collectionComparisons.toString(indentA) + "\n")

        String indentB = '\t\t\t\t'
        differentObjects.each { String id, CompareObjectsResults objectResults ->
            sb.append("${indentB}${id}:${objectResults.toString()}\n")
        }
        return sb.toString()
    }

    def getDifferences(){
        Map differences = [collectionComparisons:collectionComparisons, objectComparisons:objectComparisons]

    }
}


