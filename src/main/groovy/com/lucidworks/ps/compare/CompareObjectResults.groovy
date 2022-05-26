package com.lucidworks.ps.compare

import org.apache.log4j.Logger

class CompareObjectsResults {
    Logger log = Logger.getLogger(this.class.name);

    String objectType
    def source
    def destination
    List sourceOnlyKeys = []
    List destinationOnlyKeys = []
    List sharedKeys = []
    def differentValues

    CompareObjectsResults(String objectType, source, destination) {
        this.objectType = objectType
        this.source = source
        this.destination = destination
    }

    boolean isFunctionallyDifferent() {
        boolean different = true
        if (sourceOnlyKeys || destinationOnlyKeys) {
            log.debug "Different by keys/properties"
            different = true
        } else if (differentValues) {
            log.debug "Different by keys values: $differentValues"
            different = true
        } else {
            different = false
        }
        return different
    }
}
