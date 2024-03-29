package com.lucidworks.ps.compare

import com.lucidworks.ps.model.BaseObject
import com.lucidworks.ps.model.fusion.Application
import com.lucidworks.ps.transform.JsonObject

//import com.lucidworks.ps.fusion.Application

import org.apache.log4j.Logger

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/23/22, Monday
 * @description: helper class to compare two different Fusion Applications (or parts of)
 * @ deprecated ??
 * todo -- move to using BaseComparator ...?
 */
class FusionApplicationComparator {
    Logger log = Logger.getLogger(this.class.name);

    Application leftApp
    Application rightApp

    Map<String, CompareCollectionResults> collectionComparisons = [:]
    Map<String, CompareJsonObjectResults> objectComparisons = [:]

    public static final List<String> DEFAULTTHINGSTOCOMPARE = "configsets collections dataSources indexPipelines queryPipelines parsers blobs appkitApps features objectGroups links sparkJobs".split(' ')

    FusionApplicationComparator(Application left, Application right, def thingsToCompare = []) {
        if (!thingsToCompare) {
            thingsToCompare = DEFAULTTHINGSTOCOMPARE
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
    Map<String, CompareCollectionResults> compare(List<String> thingsToCompare = DEFAULTTHINGSTOCOMPARE, List valueDiffsToIgnore = []) {
        log.info "Compare things matching: (${thingsToCompare}) -- Value Diffs to ignore: $valueDiffsToIgnore"

        thingsToCompare.each { String thingType ->
            log.debug "Compare things of type: $thingType"
            Object leftThings = leftApp.getThings(thingType)
            Object rightThings = rightApp.getThings(thingType)

            LeftRightCollectionResults collectionResults = new LeftRightCollectionResults(thingType, valueDiffsToIgnore)

            if (leftThings && rightThings) {
                if (leftThings?.class?.name == rightThings?.class?.name) {
                    if(leftThings instanceof BaseObject){
                        // todo -- refactor to more elegant handling of BaseObject comparison, for now, just get the underlying JsonObjects to compare...
                        leftThings = ((BaseObject)leftThings).srcItems
                        rightThings = ((BaseObject)rightThings).srcItems
                    } else {
                        log.debug "Comparing things of class type: ${leftThings.getClass().simpleName}"
                    }

                    if (leftThings instanceof List) {
                        compareIds(leftThings, rightThings, collectionResults)
                        this.collectionComparisons[thingType] = collectionResults

                        collectionResults.sharedIds.each { String id ->
                            log.debug "\t\tComparing shared object with id: $id"
                            def leftObject = leftThings.find { it.id == id }
                            def rightObject = rightThings.find { it.id == id }

                            CompareJsonObjectResults objectsResults = compareJsonObjects(thingType, leftObject, rightObject)
                            collectionResults.objectsResults[id] = objectsResults
                            log.debug "Compare results: $objectsResults"
                        }
                    } else if (leftThings instanceof BaseObject) {
                        log.warn "Compare BaseObjects: ${leftThings.getClass().simpleName}"

                    } else if (leftThings instanceof Map) {
                        log.warn "TODO:: Process map (Left:${leftThings.keySet().size()}) for thing type: $thingType"
                    } else {
                        log.warn "Not a list? Is this features?..."
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

    /**
     * Compare to groups of objects (fusion app objects?).
     * These typically are maps, but can contain lists
     * @todo -- check list and gpath accessor functionality...
     * @param leftObjects
     * @param rightObjects
     * @param compareLabel
     * @return
     */
    CompareJsonObjectResults compareJsonObjects(String compareLabel, def leftObjects, def rightObjects) {
        CompareJsonObjectResults objectsResults = new CompareJsonObjectResults(compareLabel, leftObjects, rightObjects)
        List<String> leftKeyPaths = JsonObject.flatten(leftObjects, 1)
        List<String> rightKeyPaths = JsonObject.flatten(rightObjects, 1)

        def leftOnly = leftKeyPaths - rightKeyPaths
        if (leftOnly) {
            log.debug "\t\t$compareLabel) Left only ids: ${leftOnly}"
        } else {
            log.debug "\t\t$compareLabel) All left ids match right ids"
        }
        def rightOnly = rightKeyPaths - leftKeyPaths
        if (rightOnly) {
            log.debug "\t\t$compareLabel) Right only ids: ${rightOnly}"
        } else {
            log.debug "\t\t$compareLabel) All right ids match left ids"
        }
        objectsResults.leftOnlyKeys = leftOnly
        objectsResults.rightOnlyKeys = rightOnly

        List<String> shared = leftKeyPaths.intersect(rightKeyPaths)
        objectsResults.sharedKeys = shared
        shared.each {
            log.debug "\t\t$compareLabel) Compare values: $it (more code here....)"
            def leftChild = leftObjects[it]
            def rightChild = rightObjects[it]
            def valueComparisons = compareValues(leftChild, rightChild, compareLabel)
        }

        return objectsResults
    }

    /**
     * compare the given objects for functional equality
     * @param left
     * @param right
     * @param objectType
     * @return
     * @deprecated -- look at BaseComparator.compareValues() for preferred approach (?)
     */
    ComparisonResult compareValues(def left, def right, String objectType ) {
        //todo -- add logic to evaluate values of shared keys
        String diffType = ''
        String description = ''
        if (left == right) {
            description = "Values are the same: left:($left) and right:($right)"
            diffType = 'EQUAL'
        } else {
            String leftClassName = left.class.name
            String rightClassName = right.class.name

            if(leftClassName==rightClassName){
                log.info "Same class names (which is a good start..): $leftClassName: leftValue:($left) != right value:($right)"
                description =  "Values are the Different: left:($left) and right:($right)"
            } else {
                diffType = 'Different Classes'
                description = "Left class: [$leftClassName] differs from Right class: [$rightClassName]"
                log.info this
            }
        }
        ComparisonResult diff = new ComparisonResult(objectType, diffType, description)
        log.debug diff
        return diff
    }


    def getDifferences(){
        Map differences = [collectionComparisons:collectionComparisons, objectComparisons:objectComparisons]

    }

    String toString() {
        def differentObjects = objectComparisons.findAll { String id, CompareJsonObjectResults compareObjectsResults ->
            // todo -- switch to comparison and diffs
//            compareObjectsResults.isFunctionallyDifferent()
            compareObjectsResults.differences           // does this work? null and empty should be false here, meaning no differences
        }
        String indentA = '\t\t'
        StringBuilder sb = new StringBuilder()
//        sb.append("Collection type: $collectionType\n")
//        sb.append(collectionComparisons.toString(indentA) + "\n")

        String indentB = '\t\t\t\t'
        differentObjects.each { String id, CompareJsonObjectResults objectResults ->
            sb.append("${indentB}${id}:${objectResults.toString()}\n")
        }
        return sb.toString()
    }

}


