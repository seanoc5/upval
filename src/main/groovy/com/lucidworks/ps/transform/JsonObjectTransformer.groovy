package com.lucidworks.ps.transform

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/28/22, Tuesday
 * @description:
 */

class JsonObjectTransformer extends BaseTransformer {

    JsonObjectTransformer(Object source, Rules rules, Map<String, Object> destination = [:], String pathSeparator = '/') {
        super(source, rules, destination, pathSeparator)
    }

//    JsonObjectTransformer(Object source, Rules rules, Object destination) {
//        super(source, rules, destination)
//    }
//
//    JsonObjectTransformer(Object source, Rules rules) {
//        super(source, rules)
//    }

    /**
     * explicit override just for clarity/visibility, unsure if we need customization, or can leave as falling through to super method...
     * @return
     */
    @Override
    def transform() {
        return super.transform()
    }


    /**
     *
     * @param rules
     * @return
     */
    @Override
    def performCopyRules(Rules rules) {
        List results = []
        rules.copy.each { def rule ->
            log.info "\t\tCOPY rule: $rule"
            results << rule
        }
        return results
    }

    @Override
    def performSetRules(Rules rules) {
        List results = []
        rules.set.each { def rule ->
            log.info "\t\tSET rule: $rule"
            results << rule
        }
        return results
    }

    @Override
    def performRemoveRules(Rules rules) {
        List results = []
        rules.copy.each { def rule ->
            log.info "Copy rule: $rule"
            results << rule
        }
        return results

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
