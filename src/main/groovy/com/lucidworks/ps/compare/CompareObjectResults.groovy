package com.lucidworks.ps.compare

import org.apache.log4j.Logger

class CompareObjectsResults {
    Logger log = Logger.getLogger(this.class.name);

    String compareLabel
    def left
    def right
    Collection leftOnlyKeys
    Collection rightOnlyKeys
    Collection sharedKeys
    List<Comparison> differences = []
    List<Comparison> similarities = []

    CompareObjectsResults(String compareLabel, left, right) {
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
}
