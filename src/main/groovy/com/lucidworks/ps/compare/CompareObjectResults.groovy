package com.lucidworks.ps.compare

import org.apache.log4j.Logger


/**
 * Collection of many (sub)object comparisons, and some helper functionality to analyze/display differences
 * perhaps replaced with Comparison??
 * @see ComparisonResult
 */
public class CompareObjectResults {
    Logger log = Logger.getLogger(this.class.name);

    String compareLabel
    def left
    def right
    Collection leftOnlyKeys
    Collection rightOnlyKeys
    Collection sharedKeys
    List<ComparisonResult> differences = []
    List<ComparisonResult> similarities = []

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
