package com.lucidworks.ps.transform

import org.apache.log4j.Logger

/** Base class for transforming things
 * Assumes we are dealing with Fusion "object" like things, but this is still inn discovery (June 22)
 * todo-- decide if we keep this, augment it, or drop
 */
abstract class BaseTransformer {
    static Logger log = Logger.getLogger(this.class.name);
    /** JsonSlurped object (maps/lists) to read from */
    def sourceObject
    /** Maps/lists object that is the goal of the transformation */
    def destinationObject = [:]
    /** Rules object that provides structure to a collection of transform rules */
//    Rules rules
    String separator

    abstract def srcFlatpaths
    // these need to be parsed base on type of source/dest objects (i.e. based on descendant class type)
    abstract def destFlatpaths

    BaseTransformer() {
    }

//    abstract BaseTransformer(def source, Rules rules, Map<String, Object> destination, String pathSeparator)
    BaseTransformer(def source, Map<String, Object> destination = [:], String pathSeparator = '/') {
        this.sourceObject = source
        this.destinationObject = destination
//        this.rules = rules
        this.separator = separator
    }

    /**
     * main action to perform series of transformations
     * todo -- fix creating missing hierarchies
     */
    def transform(def rules) {
        Map<String, Object> results = [:]

        if (!destinationObject) {
            if (rules.copy) {
                log.info "\t\tNo destination object (template) given, but we DO HAVE copy rules (${rules.copy}, so we are NOT cloning the default as a destination template"
            } else {
//                log.info "\t\tNo destination object (template) given, but we do NOT have copy rules (${rules.copy}, so clone the source as a template for the destination..."
//                destinationObject = sourceObject.clone()
            }
        } else {
            log.info "transformer.transform() was given both source and destination objects, no cloning needed..."
        }

        if (rules.copy) {
            results.copyResults = performCopyRules(rules.copy)
            log.info "COPY rules (${rules.copy}) -> results: ${results.copyResults?.size()}"
        } else {
            log.info "No COPY rules, skipping..."
        }

        if (rules.set) {
            results.setResults = performSetRules(rules.set)
            log.info "SET (${rules.set}) -> results: ${results.setResults?.size()}"
        } else {
            log.info "No SET rules, skipping..."
        }

        if (rules.remove) {
            results.removeResults = performRemoveRules(rules.remove)
            log.info "REMOVE (${rules.remove}) -> results: ${results.removeResults?.size()}"
        } else {
            log.info "No REMOVE rules, skipping..."
        }

        return results
    }


    /**
     * process all of the given 'copy' rules
     * @param rules
     * @return
     */
    def performCopyRules(List<Map> rules) {
        List results = []
        rules.copy.each { def rule ->
            log.info "\t\tCOPY rule: $rule"
//            sourceObject
            results << rule
        }
        return results

    }

    /**
     * process all of the given 'sest' rules
     * @param rules
     * @return
     */
    def performSetRules(def rules) {
        List results = []
        rules.each { def rule ->
            log.info "\t\tSET rule: $rule"
            results << rule
        }
        return results


    }

    /**
     * process all of the given 'remove' rules
     * @param rules
     * @return
     */
    def performRemoveRules(def rules) {
        List results = []
        rules.remove.each { def rule ->
            log.info "\t\tREMOVE rule: $rule"
            sourceObject
            results << rule
        }
        return results

    }


    /**
     * process single 'copy' rules
     * @param rules
     * @return
     */
    abstract def doCopy(def valueToSet, List<String> destNodePaths)

    /**
     * process all of the given 'sest' rules
     * @param rules
     * @return
     */
    abstract def doSet(def valueToSet, List<String> destNodePaths)

    /**
     * process all of the given 'remove' rules
     * @param rules
     * @return
     */
    abstract def doRemove(List<String> destNodePaths)

//    abstract def getDestinationValue(String pi
//    def getSourceValue(def path) {
//        log.debug "Get source object value from path: $path"
//        def result
//        try {
//            result = evalObjectPathExpression(sourceObject, path)
//        } catch (Exception e){
//            log.warn "getSourceValue: Exception: $e"
//        }
//        return result
//    }
//    def getDestinationValue(def path) {
//        log.debug "Get destination object value from path: $path"
//        def result
//        try {
//            result = evalObjectPathExpression(destinationObject, path)
//            if(!result){
//                log.info "No value found for path: $path"
//            }
//        } catch (Exception e){
//            log.warn "getDestinationValue: Exception: $e"
//        }
//        return result
//    }

    /** optional static method to get a 'thing', intention is to get a node rather than a leaf value, so more code here?
     * todo - add code/logic to get a node rather than a value?... this is likely only necessary if the 'set' value code doesn't work correctly...?
     * @param path
     * @param object
     */
//    abstract getNode(def path, Object object) {
//    static getNode(def path, Object object) {
//        log.info "Get Node from path: $path in object type: ${object.getClass().simpleName} "
//        def result = evalObjectPathExpression(object, path)
//    }

//    def getNodeParent(def path) {
//        log.warn "more code here"
//    }

    /**
     * convenience method to set source object value (rarely needed/used?)
     * @param path
     * @param valueToSet
     */
//    def setSourceValue(def path, def valueToSet) {
//        String exp = "${path}=${valueToSet}"
//        log.warn "set source value with Eval.me expression: $exp"
//        def rslt = evalObjectPathExpression(sourceObject, exp)
//        return rslt
//    }

    /**
     * convenience method to set destinationObject object value (rarely needed/used?)
     * @param path
     * @param valueToSet
     */
//    def setDestinationValue(def path, def valueToSet) {
//        String exp
//        if(valueToSet instanceof Number) {
//            exp = "${path}=${valueToSet}"
//        } else if(valueToSet instanceof String) {
//            exp = "${path}=\"${valueToSet}\""
//            log.info "\t\tString: valToSet object type: ${valueToSet.getClass().simpleName}, add quotes to value in exp: $exp"
////        } else if(valueToSet instanceof String){
//        } else {
//            log.warn "\t\tUnknown valToSet object type: ${valueToSet.getClass().simpleName}"
//            exp = "${path}=\"${valueToSet}\""
//
//        }
//        log.warn "set destinationObject value with Eval.me expression: $exp"
//        def rslt = evalObjectPathExpression(destinationObject, exp)
//        return rslt
//    }


}
