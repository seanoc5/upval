package com.lucidworks.ps.compare

/**
 * outdated approach? please review and refactor or revive as necessary...
 * @see BaseComparator
 * @see ComparisonResult
 */
class LeftRightCollectionResults {
    public static final String SOURCE = 'source'
    public static final String DESTINATION = 'destination'

    String collectionType = 'unknown'
    Integer countDifference = 0
    List<String> leftOnlyIds = []
    List<String> rightOnlyIds = []
    List<String> sharedIds = []
    List leftOnlyItems = []
    List rightOnlyItems = []

    Map<String, CompareJsonObjectResults> objectsResults = [:]
    List<String> leftOnlyPaths = []
    List<String> rightOnlyPaths = []

    List<Object> similarEntries = []
    List<Object> differentEntries = []

    /**
     * list of patterns to consider 'OK' if only the values differ, e.g. username, url, zkhost values...
     */
    List ignoreDifferentValueEntries = []

    /**
     * typical constructor, accepting a list of object property paths to consider "the same" if onlly values differ (but structure is equal)
     * @param valueDiffsToIgnore
     */
    LeftRightCollectionResults(String collectionType, List valueDiffsToIgnore) {
        this.collectionType = collectionType
        this.ignoreDifferentValueEntries = valueDiffsToIgnore
    }

    String toString(String indent='\t\t'){
        StringBuilder sb = new StringBuilder()
        sb.append("Collection Type: $collectionType\n")
        if(countDifference==0){
            sb.append("${indent}Count difference ($countDifference)\n")
        }
        if(leftOnlyIds){
            sb.append("${indent}Source only IDs: $leftOnlyIds\n")
        }
        if(rightOnlyIds){
            sb.append("${indent}Destination only IDs: $rightOnlyIds\n")
        }
        return sb.toString()
    }
}
