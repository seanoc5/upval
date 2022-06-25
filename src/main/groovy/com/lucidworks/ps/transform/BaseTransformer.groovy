package com.lucidworks.ps.transform

import org.apache.log4j.Logger

/** Base class for transforming things
 * Assumes we are dealing with Fusion "object" like things, but this is still inn discovery (June 22)
 * todo-- decide if we keep this, augment it, or drop
 */
class BaseTransformer {
    static Logger log = Logger.getLogger(this.class.name);
    /** JsonSlurped object (maps/lists) to read from */
    def sourceObject
    /** Maps/lists object that is the goal of the transformation */
    def destinationObject
    /** Rules object that provides structure to a collection of transform rules */
    Rules rules

    BaseTransformer(def source, Rules rules, def destination = [:], String pathSeparator = '/') {
        this.sourceObject = source
        this.destinationObject = destination
        this.rules = rules
    }

    /**
     * main action to perform series of transformations
     */
    def transform() {
        log.info "COPY results: $copyResult"
        def copyResult = performCopyRules(rules)

        log.info "SET results: $copyResult"
        def setResult = performSetRules(rules)

        log.info "REMOVE results: $copyResult"
        def removeResult = performRemoveRules(rules)

    }

    def getSourceValue(def path) {
        log.debug "Get source object value from path: $path"
        def result
        try {
            result = evalObjectPathExpression(sourceObject, path)
        } catch (Exception e){
            log.warn "getSourceValue: Exception: $e"
        }
        return result

    }

    def getDestinationValue(def path) {
        log.debug "Get destination object value from path: $path"
        def result
        try {
            result = evalObjectPathExpression(destinationObject, path)
            if(!result){
                log.info "No value found for path: $path"
            }
        } catch (Exception e){
            log.warn "getDestinationValue: Exception: $e"
        }
        return result
    }

    /** optional static method to get a 'thing', intention is to get a node rather than a leaf value, so more code here?
     * todo - add code/logic to get a node rather than a value?... this is likely only necessary if the 'set' value code doesn't work correctly...?
     * @param path
     * @param object
     */
    static getNode(def path, Object object) {
        log.info "Get Node from path: $path in object type: ${object.getClass().simpleName} "
        def result = evalObjectPathExpression(object, path)
    }

    def getNodeParent(def path) {
        log.warn "more code here"
    }

    /**
     * convenience method to set source object value (rarely needed/used?)
     * @param path
     * @param valueToSet
     */
    def setSourceValue(def path, def valueToSet) {
        String exp = "${path}=${valueToSet}"
        log.warn "set source value with Eval.me expression: $exp"
        def rslt = evalObjectPathExpression(sourceObject, exp)
        return rslt
    }

    /**
     * convenience method to set destinationObject object value (rarely needed/used?)
     * @param path
     * @param valueToSet
     */
    def setDestinationValue(def path, def valueToSet) {
        String exp
        if(valueToSet instanceof Number) {
            exp = "${path}=${valueToSet}"
        } else if(valueToSet instanceof String) {
            exp = "${path}=\"${valueToSet}\""
            log.info "\t\tString: valToSet object type: ${valueToSet.getClass().simpleName}, add quotes to value in exp: $exp"
//        } else if(valueToSet instanceof String){
        } else {
            log.warn "\t\tUnknown valToSet object type: ${valueToSet.getClass().simpleName}"
            exp = "${path}=\"${valueToSet}\""

        }
        log.warn "set destinationObject value with Eval.me expression: $exp"
        def rslt = evalObjectPathExpression(destinationObject, exp)
        return rslt
    }


    /**
     * quick and dirty ploceholder function to operate like GPath or JsonPath
     * @param slurpedJsonObject the object to perform expression on (Map/Collection combination -from JsonSlurpoer)
     * @param expr the string to evaluate: a Gpath-like string
     * @return result of evaluation (read value, or result of setting...)
     *
     * @note this will likely only set string values -- is this a blocker?
     * @see groovy.json.JsonSlurper
     */
    static Object evalObjectPathExpression(Object o, String expr, String root = 'ROOT') throws IllegalArgumentException {
        def result
        log.info "\t\tEval expression($expr) on object (${o instanceof Map ? o.keySet() : o})"

        String pathExpression = expr.trim()
        if (o && pathExpression) {

            if (pathExpression.startsWith('/')) {
                List<String> parts = pathExpression.split('/')
                if (parts[0] == '') {
                    log.info "set start of path expression to root: $root..."
                    parts[0] = root
                }
                pathExpression = parts.join('.')
                log.info "Converted slashy expression ($expr) to gpath-like expression for groovy Eval.me -- starts with 'root[${root}].' => $pathExpression"
            } else if(pathExpression.startsWith(root)){
                log.debug "We seem to have a properly (pre)formatted path, nothing to adjust..."
            } else {
                pathExpression = "${root}.$pathExpression"
                log.debug "No leading root($root) in expr path($expr), add it to pathExpression($pathExpression) -- (needed for Eval.me?)..."
            }

            try {
                result = Eval.me('ROOT', o, pathExpression)
            } catch (MissingPropertyException mpe) {
                log.warn "Problem evaluating (Eval.me) in expression: ($pathExpression), returning null\n\t\tException:$mpe "
            }
        } else {
            String msg = "Invalid object($o) or path expression($pathExpression)"
            log.warn msg
            throw new IllegalArgumentException(msg)
        }
        return result
    }


    /**
     * process all of the given 'copy' rules
     * @param rules
     * @return
     */
    def performCopyRules(Rules rules) {
        List results = []
        rules.copy.each { def rule ->
            log.info "\t\tCOPY rule: $rule"
            results << rule
        }
        return results
    }

    /**
     * process all of the given 'sest' rules
     * @param rules
     * @return
     */
    def performSetRules(Rules rules) {
        List results = []
        rules.set.each { def rule ->
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
    def performRemoveRules(Rules rules) {
        List results = []
        rules.copy.each { def rule ->
            log.info "Copy rule: $rule"
            results << rule
        }
        return results
    }
}
