package com.lucidworks.ps.compare

import org.apache.log4j.Logger

/**
 * Base Object to track and report on comparison results
 */
class ComparisonResult {
    Logger log = Logger.getLogger(this.class.name)

    public static final String EQUAL = 'EQUAL'
    /** Similar typically used for comparisons that ignore value differences, just look at structure */
    public static final String SIMILAR = 'SIMILAR'

    public static final String DIFF_LEFT_ONLY = 'Left Only items'
    public static final String DIFF_RIGHT_ONLY = 'Right Only items'
    public static final String DIFF_VALUES = 'Different Values'
    public static final String DIFF_CLASSES = 'Different Classes'
//    public static final String DIFF_CHILDREN = 'Different Children'

    String compareLabel
    String differenceType           // elements/structure/keys, values, order
    String description

    // todo -- consider include pattern of value differences to ignore here?? or in comparator?

    ComparisonResult(String compareLabel, String differenceType, String description) {
        this.compareLabel = compareLabel
        this.differenceType = differenceType
        this.description = description
    }

    @Override
    public String toString() {
        String s = null
        if(compareLabel){
            s = "Compare:(${compareLabel.padLeft(20)}) [${differenceType?.padLeft(20)}]:: ${description}"
        } else {
            s = "Compare:(${'no label given?'.padLeft(20)}) [${differenceType?.padLeft(20)}]:: ${description}"
        }
        return s
    }

    boolean isDifferent() {
        if (this.differenceType == EQUAL) {
            log.debug "\t\t Values are equal"
            return false

        } else if (this.differenceType == SIMILAR) {
            log.info "\t\t Objects are Similar (typically true if we are ignoring value differences, just looking at structure): ${this.toString()}"
            return false

        } else {
            return true
        }
    }
}
