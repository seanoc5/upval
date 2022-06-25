package com.lucidworks.ps.transform

import org.apache.log4j.Logger

/** Base class for transforming things
 * Assumes we are dealing with Fusion "object" like things, but this is still inn discovery (June 22)
 * todo-- decide if we keep this, augment it, or drop
 */
class BaseTransformer {
    Logger log = Logger.getLogger(this.class.name);
    def source
    def destination
    Rules rules

    BaseTransformer(def source, Rules rules, def destination) {
        this.source = source
        this.destination = destination
        this.rules = rules
    }
/**
     * main action to perform series of transformations
     */
    def transform(){
        def copyResult = performCopyRules(rules)
        log.info "COPY results: $copyResult"

        def setResult = performSetRules(rules)
        log.info "SET results: $copyResult"

        def removeResult = performRemoveRules(rules)
        log.info "REMOVE results: $copyResult"

    }

    def getSourceValue(def path){
        log.warn "more code here"
    }

    def getDestinationValue(def path){
        log.warn "more code here"
    }

    def getNode(def path){
        log.warn "more code here"
    }

    def getNodeParent(def path){
        log.warn "more code here"
    }

    def setSourceValue(def path, def valueToSet){
        log.warn "more code here"
        String exp = "${path}=${valueToSet}"
        def rslt = evalObjectPathExpression(source, exp)
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
    def evalObjectPathExpression(Object slurpedJsonObject, String expr) {
        return Eval.me('ROOT', slurpedJsonObject, expr)
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
