package com.lucidworks.ps.compare

import com.lucidworks.ps.upval.Helper
import org.apache.log4j.Logger

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/23/22, Monday
 * @description: helper class to compare two different Lists of datasources
 * we use the terminology source and destination losely, but the aim is to help understand the difference reporting
 */

class FusionObjectComparator {
    Logger log = Logger.getLogger(this.class.name);

    String collectionType
    List<Map> source = []
    List<Map> destination = []
    CompareCollectionResults compareCollections
    Map<String, CompareObjectsResults> compareObjectsResultsMap = [:]

    FusionObjectComparator(String collectionType, List<Map<String, Object>> src, List<Map<String, Object>> dest) {
        log.info "Starting collection (type: $collectionType) comparison with (${source.size()}) source objects, and (${destination.size()}) destination objections..."
        this.collectionType = collectionType
        source = src
        destination = dest
        List ignoreValueDifferences = []
        compareCollections = new CompareCollectionResults(collectionType, ignoreValueDifferences)
        compare()
    }

    CompareCollectionResults compare() {
        Integer diffCount = compareItemCounts()

        Map diffMap = compareIds()

        compareCollections.sharedIds.each { String id ->
            log.info "Comparing shared object with id: $id"
            def srcObject = source.find {it.id == id}
            def destObject = destination.find{it.id == id}
            CompareObjectsResults objectsResults = compareObjects(srcObject, destObject)
            compareObjectsResultsMap[id] = objectsResults
            log.debug "Compare results: $objectsResults"
        }

        return compareCollections
    }

    Integer compareItemCounts() {
        compareCollections.countDifference = source.size() - destination.size()
        return compareCollections.countDifference
    }

    Map<String, List<String>> compareIds() {
        def srcIds = source.collect { it.id }
        def destIds = destination.collect { it.id }

        compareCollections.sourceOnlyIds = srcIds - destIds
        compareCollections.destinationOnlyIds = destIds - srcIds
        compareCollections.sharedIds = srcIds.intersect(destIds)

        compareCollections.sourceOnlyItems = source.findAll { compareCollections.sourceOnlyIds.contains(it.id) }
        compareCollections.destinationOnlyItems = destination.findAll { compareCollections.destinationOnlyIds.contains(it.id) }
        Map diffMap = [sourceOnly: compareCollections.sourceOnlyIds, destinationOnly: compareCollections.destinationOnlyIds]
        return diffMap
    }

    CompareObjectsResults compareObjects(def src, def dest) {
        CompareObjectsResults objectsResults = new CompareObjectsResults(collectionType, src, dest)
        def srcKeyPaths = Helper.flatten(src, 1)
        def destKeyPaths = Helper.flatten(dest, 1)

        def srcOnly = srcKeyPaths - destKeyPaths
        def destOnly = destKeyPaths - srcKeyPaths
        objectsResults.sourceOnlyKeys = srcOnly
        objectsResults.destinationOnlyKeys = destOnly

        def shared = srcKeyPaths.intersect(destKeyPaths)
        objectsResults.sharedKeys = shared

        return objectsResults
    }

    String toString(){
        def differentObjects = compareObjectsResultsMap.findAll {String id, CompareObjectsResults objectsResults->
            objectsResults.isFunctionallyDifferent()
        }
        String indentA = '\t\t'
        StringBuilder sb = new StringBuilder()
        sb.append("Collection type: $collectionType\n")
        sb.append(compareCollections.toString(indentA) + "\n")

        String indentB = '\t\t\t\t'
        differentObjects.each { String id, CompareObjectsResults objectResults ->
            sb.append("${indentB}${id}:${objectResults.toString()}\n")
        }
        return sb.toString()
    }
}


