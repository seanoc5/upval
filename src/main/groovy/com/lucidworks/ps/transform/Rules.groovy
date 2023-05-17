//package com.lucidworks.ps.transform
//
///**
// * @author :    sean
// * @mailto :    seanoc5@gmail.com
// * @created :   6/24/22, Friday
// * @description:
// */
//
///**
// * wrapper class to provide structure/guidance to rules for transformation
// * <li>copy: copy values from source to dest (regex patterns?)
// * <li>set: rule to set the destination value regardless of source/copy
// * <li>remove: rules to remove anything in destination that might have been copied, set, or in the original template
// * <p>
// * todo -- flesh out code here
// */
//class Rules {
//    /** rules to copy values from sourceobject (flattened path) to destination object (if no destination path, use sourcepath)
//     * a copy rule can be a single String or Pattern, which instructs copying from sourceMap to destmap with the same path
//     * */
//    Map copy
//    /** rules to set values in destination map (with static or `eval`-ed value) */
//    Map set
//    /** rules to remove values after copy/set are performed */
//    List remove
//
//    Rules(Map copy, Map set, List remove) {
//        this.copy = copy
//        this.set = set
//        this.remove = remove
//    }
//
//    /**
//     * potential convenience method to load rules from (JsonSlurped) map
//     * @param rulesMap
//     */
//    Rules(Map rulesMap){
////        this.copy = rulesMap.copy
////        this.set = rulesMap.set
////        this.remove = rulesMap.remove
//    }
//}
