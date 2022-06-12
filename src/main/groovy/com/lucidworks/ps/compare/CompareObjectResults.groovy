package com.lucidworks.ps.compare

import org.apache.log4j.Logger

class CompareObjectsResults {
    Logger log = Logger.getLogger(this.class.name);

    String objectType
    def left
    def right
    List leftOnlyKeys = []
    List rightOnlyKeys = []
    List sharedKeys = []
    List<Difference> differences
    boolean isDifferent

    CompareObjectsResults(String objectType, left, right) {
        this.objectType = objectType
        this.left = left
        this.right = right
        this.isDifferent = isFunctionallyDifferent()
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
