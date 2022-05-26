package com.lucidworks.ps.compare

class CompareCollectionResults {
    public static final String SOURCE = 'source'
    public static final String DESTINATION = 'destination'

    String collectionType = 'unknown'
    Integer countDifference = 0
    List<String> sourceOnlyIds = []
    List<String> destinationOnlyIds = []
    List<String> sharedIds = []
    List sourceOnlyItems = []
    List destinationOnlyItems = []
    List<String> sourceOnlyPaths = []
    List<String> destinationOnlyPaths = []

    List<Object> similarEntries = []
    List<Object> differentEntries = []

    /**
     * list of patterns to consider 'OK' if only the values differ, e.g. username, url, zkhost values...
     */
    List ignoreDifferentValueEntries = []

    /**
     * typical constructor, accepting a list of object property paths to consider "the same" if onlly values differ (but structure is equal)
     * @param ignoreValueDifferences
     */
    CompareCollectionResults(String collectionType, List ignoreValueDifferences) {
        this.collectionType = collectionType
        this.ignoreDifferentValueEntries = ignoreValueDifferences
    }

    String toString(String indent='\t\t'){
        StringBuilder sb = new StringBuilder()
        sb.append("Collection Type: $collectionType\n")
        if(countDifference==0){
            sb.append("${indent}Count difference ($countDifference)\n")
        }
        if(sourceOnlyIds){
            sb.append("${indent}Source only IDs: $sourceOnlyIds\n")
        }
        if(destinationOnlyIds){
            sb.append("${indent}Destination only IDs: $destinationOnlyIds\n")
        }
    }
}
