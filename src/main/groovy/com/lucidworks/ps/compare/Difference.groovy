package com.lucidworks.ps.compare

import org.apache.log4j.Logger

/**
 * Base Object to track and report on comparison diffferences
 */
class Difference {
    public static final String DIFF_LEFT_ONLY = 'Left Only items'
    public static final String DIFF_RIGHT_ONLY = 'Right Only items'
    public static final String DIFF_VALUES = 'Different Values'
    Logger log = Logger.getLogger(this.class.name);
    String objectType
    String differenceType           // elements/structure/keys, values, order
    String description

    // todo -- consider include pattern of value differences to ignore here?? or in comparator?

    Difference(String objectType, String differenceType, String description) {
        this.objectType = objectType
        this.differenceType = differenceType
        this.description = description
    }

    @Override
    public String toString() {
        return "Difference{" +
                "objectType='" + objectType + '\'' +
                ", differenceType='" + differenceType + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
