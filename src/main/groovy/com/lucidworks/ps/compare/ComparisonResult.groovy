package com.lucidworks.ps.compare

class ComparisonResult {
    String compareLabel = 'unknown'
    Boolean equal
    String message
    Collection differences

    ComparisonResult(String label, Boolean equal, String message, Collection differences) {
        compareLabel = label
        this.equal = equal
        this.message = message
        this.differences = differences
    }

    ComparisonResult(String label, Boolean equal, String message) {
        compareLabel = label
        this.equal = equal
        this.message = message
    }


    @Override
    public String toString() {
        String s = null
        if (differences) {
            s = "$compareLabel) Equal? '$equal' -> $message='${message} => Differences: $differences"
        } else {
            s = "$compareLabel) EQUAL($equal) -> $message='${message}"
        }
        return s
    }
}
