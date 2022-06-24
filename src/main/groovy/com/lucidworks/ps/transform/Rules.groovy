package com.lucidworks.ps.transform

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/24/22, Friday
 * @description:
 */

/**
 * wrapper class to provide structure/guidance to rules for transformation
 * todo -- flesh out code here
 */
class Rules {
    /** rules to copy values from sourceobject (flattened path) to destination opbject (if no destination path, use sourcepath)
     * a copy rule can be a single String or Pattern, which instructs copying from sourceMap to destmap with the same path
     * a copy rule with two entries will copy from Map items matching first entry, to destMap items matching second entry
     * */
    List copy
    /** rules to set values in destination map (with static or `eval`-ed value) */
    List set
    /** rules to remove values after copy/set are performed */
    List remove

    Rules(List copy, List set, List remove) {
        this.copy = copy
        this.set = set
        this.remove = remove
    }

    /**
     * potential convenience method to load rules from (JsonSlurped) map
     * @param rulesMap
     */
    Rules(Map rulesMap){
        this.copy = rulesMap.copy
        this.set = rulesMap.set
        this.remove = rulesMap.remove
    }
}
