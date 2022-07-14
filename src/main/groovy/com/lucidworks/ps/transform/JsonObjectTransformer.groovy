package com.lucidworks.ps.transform

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/28/22, Tuesday
 * @description:
 */

class JsonObjectTransformer extends BaseTransformer {

    JsonObjectTransformer(Object source, Map<String, Object> destination = [:], String pathSeparator = '/') {
        if (checkSourceDestinationTypesAreValid(source, destination)) {
            this.sourceObject = source
            this.destinationObject = destination
//            this.rules = rules
            this.separator = separator
            if(!destination) {
                log.info "Does this clone work? Do we need to implement a process to: create and then deep copy??"
                destination = source.clone()
            }
            // get the flattened paths of the source and dest json objects, for the transform to use below
            srcFlatpaths = JsonObject.flattenWithLeafObject(source)
            destFlatpaths = JsonObject.flattenWithLeafObject(destination)

        } else {
            String msg = "Source and destination are not both valid, throwing error"
            log.warn msg
            throw new IllegalArgumentException(msg)
        }
    }

    /**
     * check basics of transformation objects, this may grow in scope to handle other types of objects, but for Json transformations, this should be all we need...?
     * note: is it possible to do a transformation with a null destination???
     * @param source jsonslurped object (map or list)
     * @param destination jsonslurped object (map or list)
     * @return true for valid source/destination
     */
    boolean checkSourceDestinationTypesAreValid(Object source, Object destination) {
        boolean isvalid = false
        if (source instanceof Map || source instanceof Collection) {
            if (destination == null || destination instanceof Map || destination instanceof Collection) {
                isvalid = true
            } else {
                log.warn "Source object type (${source.getClass().simpleName}) seems valid, but destination type (${destination.getClass().simpleName}) was not, probably should cancel transformation..."
                isvalid = false
            }
        }
        return isvalid
    }

//    @Override
//    def performCopyRules(Object rules) {
//        List results = []
//             rules.each { def rule ->
//                 log.info "\t\tCOPY rule: $rule"
//                 results << rule
//             }
//             return results
//    }

    @Override
    def performSetRules(Object rules) {
//        return super.performSetRules(rules)
        List results = []
             rules.each { def rule ->
                 log.info "\t\tset rule: $rule"
     //            sourceObject
                 results << rule
             }
             return results
    }

    @Override
    def performRemoveRules(def rules) {
        List results = []
        rules.remove.each { def rule ->
            log.info "Remove rule: $rule"
            results << rule
        }
        return results

    }

    @Override
    def doCopy(def valToSet, def destNodes) {
        return doSet(valToSet, destNodes)
    }

    @Override
    def doSet(def valToSet, def destNodes) {
        log.warn "Implement me!! blank operation at the moment"
        return null
    }

    @Override
    def doRemove(def destNodes) {
        log.warn "Implement me!! blank operation at the moment"
        return null
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
            } else if (pathExpression.startsWith(root)) {
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

}
