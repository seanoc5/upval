package com.lucidworks.ps.compare

import org.apache.log4j.Logger

/**
 * early attempt to compare objects, perhaps replaced with @see:Base
 * @deprecated
 */
class CompareObjectResults {
    Logger log = Logger.getLogger(this.class.name);

    String compareLabel
    def left
    def right
    Collection leftOnlyKeys
    Collection rightOnlyKeys
    Collection sharedKeys
    List<Comparison> differences = []
    List<Comparison> similarities = []

    CompareObjectResults(String compareLabel, left, right) {
        this.compareLabel = compareLabel
        this.left = left
        this.right = right
    }

    /**
     * add code here to increase logic
     * @return
     */
    boolean isFunctionallyDifferent() {
        boolean different = true
        if (leftOnlyKeys || rightOnlyKeys) {
            log.debug "Different by keys/properties"
            different = true
        } else if (differences) {
            log.debug "Different by keys values: $differences"
            different = true
        } else {
            different = false
        }
        return different
    }


    @Override
    public String toString() {
        String diffs = differences.collect{"\n\t\t$it"}
        return "CompareObjectResults(${compareLabel})" +
                "\n\tleftOnlyKeys(${leftOnlyKeys})" +
                "\n\trightOnlyKeys:(${rightOnlyKeys})" +
                "\n\tdifferences:(${diffs})" +
                "\n\tsharedKeys count:(${sharedKeys.size()}) " +
                "\n\tsimilarities:(${similarities})"
    }
}
