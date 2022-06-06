package com.lucidworks.ps.compare

class CompareCollectionResults {
//    public static final String LEFT = 'left'
//    public static final String RIGHT = 'right'

    String collectionType = 'unknown'
    Integer countDifference = 0
    def left
    def right
    List<String> leftOnlyIds = []
    List<String> rightOnlyIds = []
    List<String> sharedIds = []
    List leftOnlyItems = []
    List rightOnlyItems = []

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
    CompareCollectionResults(String collectionType, def left, def right, List ignoreValueDifferences) {
        this.collectionType = collectionType
        this.left = left
        this.right = right
        this.ignoreDifferentValueEntries = ignoreValueDifferences
    }

    String toString(String indent = '\t\t') {
        StringBuilder sb = new StringBuilder()
        sb.append("Collection Type: $collectionType\n")
        if (countDifference == 0) {
            sb.append("${indent}Count difference ($countDifference)\n")
        }
        if (leftOnlyIds) {
            sb.append("${indent}left only IDs: $leftOnlyIds\n")
        }
        if (rightOnlyIds) {
            sb.append("${indent}right only IDs: $rightOnlyIds\n")
        }
    }

    def getSimilarities() {
        return similarEntries
    }

    def getDifferences() {
        return differentEntries
    }
}
