package com.lucidworks.ps.transform

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import org.apache.log4j.Logger

import java.text.SimpleDateFormat

/**
 * Use <a href='https://github.com/json-path/JsonPath'>Jayway</a> based transformer to apply rules to a JsonSlurper.parse* object (i.e. map of maps/lists)
 * todo -- handle an initial list rather than the assumed map (with possible lists as children)
 * Class to perform transfomations on JSON slurped
 * @see groovy.json.JsonSlurper
 * todo improve class design - consider implementing an interface, or perhaps a base transformer factory to allow abstraction of implementation choice
 * todo should this handle xml sources as well, or keep that functionality distinct? xml nodes are significantly different that JsonSlurper maps/lists
 * <a href='https://github.com/json-path/JsonPath'>Jayway</a> based transformer
 *
 */
class ObjectTransformerJayway {
    static Logger log = Logger.getLogger(this.class.name);
    Map sourceMap
    DocumentContext srcContext

    Map destinationMap
    DocumentContext destContext

    Map rules

    /**
     * @deprecated
     * todo -- working on moving to functional approach (static calls)
     */
    ObjectTransformerJayway(Map srcMap, Map destMap, Map rules) {
        log.debug "Constructor: (Map srcMap, Map destMap, Map transformConfig, String pathSeparator)..."
        sourceMap = srcMap
        destinationMap = destMap

        srcContext = JsonPath.parse(sourceMap)
        destContext = JsonPath.parse(destinationMap)

        this.rules = rules
    }

    /**
     * process the configuration rules, currently `set` and `copy`
     * @return
     * todo -- remove me, moving to functional-friendly approach (static calls)
     * @deprecated
     */
    Map<String, List<Map<String, Object>>> transform() {
        Map resultsMap = [:]
        List<Map<String, Object>> myset = setValues()
        resultsMap['set'] = myset
        List<Map<String, Object>> myCopy = copyValues()
        resultsMap['copy'] = myCopy
        return destinationMap
    }

    /**
     * Static (functional-friendly?) method to apply transform rules
     * @param srcMap map of elements (can contain list children, yes?) to transform, with optional destination template (for default 'new' values)
     * @param rules map of rule types (copy,set,remove,...?) and patterns to match
     * <br><b>Note:</b>
     * <ul> <li>'copy' rules will match flattened-path elements (regex pattern) and copy the value there to the same path in the destination map</li>
     * <li>'set' rules will set (creating elements and parents as necessary) the given value in the dest map</li>
     * <li>'remove' rules will remove elements in the dest map (generated from bulk copy command, or present but not needed in dest template
     * </li>
     * @param destinationTemplate an optional destination template of values with default values
     * @return transformed map, which will be suitable for JsonOutput or other Fusion-friendly output efforts
     *
     * Note: current assumption is that the rules will be processed in the order of: copy -> set -> remove to allow for bulk copying, overriden by explicit sets, and remove commands have final authority
     */
    static Map<String, List<Map<String, Object>>> transform(Map srcMap, Map rules, Map destinationTemplate = [:]) {
        Map resultsMap = destinationTemplate

        // ---------- COPY -------------
        List<Map<String, Object>> myCopy = copyValues(srcMap, rules, destinationTemplate)
        resultsMap = resultsMap + myCopy

        // ---------- SET -------------
        List<Map<String, Object>> myset = setValues(srcMap, rules, destinationTemplate)
        resultsMap = resultsMap + myset

        // ---------- REMOVE -------------
        if(rules.remove){
            log.warn "Untested code/functionality at the moment... write tests, fix broken stuff... remove rules: ${rules.remove}"
        }
        List<Map<String, Object>> myRemove = removeItems(srcMap, rules, destinationTemplate)


        return resultsMap

    }


    /**
     * run through 'set' rules and set destination values based on these rules
     * <br>(with the `$` to signal groovy evaluation of the rule value)
     * @return list of rule results
     */
    static List<Map<String, Object>> setValues(Map srcMap, Map rules, Map destinationTemplate = [:]) {
        List<Map<String, Object>> changes = []
        def setRules = rules['set']
        DocumentContext srcContext = JsonPath.parse(srcMap)
        DocumentContext destContext = JsonPath.parse(destinationTemplate)

        setRules.each { String destPath, String value ->
            String valToSet = evaluateValue(value)

            try {
                String origDestValue = destContext.read(destPath)
                log.info "\t\toverriding orignal dest value ($origDestValue) with set value ($valToSet)"
            } catch (PathNotFoundException pnfe) {
                log.warn "Path wasn't found: $pnfe"
                log.debug "test"
            }

            // make the change here...
            DocumentContext dc = destContext.set(destPath, valToSet)
            // confirm the updated value here (optional??)
            String newDestValue = destContext.read(destPath)
            Map change = [srcValue: value, destPath: destPath, origDestValue: origDestValue, newDestValue: newDestValue]
            log.info "\t\tSet '$destPath' in destinationMap to  rule value: '$value'  -- returned doc context:$dc"
            changes << change
        }
        return changes
    }

    /**
     * Copy values from source to destination with an optional destinationTemplate holding defaults
     * @param srcMap source values to copy from
     * @param rules to dictate what to copy (from source to destinationTemplate (same path -- s)
     * @param destinationTemplate
     * @return change list (destinationTemplate is created/updated as we go...?
     */
    static List<Map<String, Object>> copyValues(Map srcMap, Map rules, Map destinationTemplate = [:]) {
        List<Map<String, Object>> changes = []
        DocumentContext srcContext = JsonPath.parse(srcMap)
        DocumentContext destContext = JsonPath.parse(destinationTemplate)

        def copyRules = rules['copy']
        log.info "\t\tCopy rules: $copyRules"

        copyRules.each { def rule ->        // String destPath, String srcPath ->
            log.info "\t\tRule: $rule"
            changes << performCopyRule(rule)
        }
        return changes
    }

    /**
     * remove items from the destination map (after copy & set rules)
     * @param dest map of fusion things after transformation
     * @param rules full set of rules to work with, this method will pick the relevant 'remove' items
     * @return modified dest object (map currently)
     *
     * Note; this is as-yet untested, todo - write some tests
     *  todo - consider a 'top' level collection rather than assume it is always a map
     */
    static List<Map<String, Object>> removeItems(Map dest, Map rules) {
        List removeRules = rules['remove']
        removeRules.each { String removePath ->
            def incomingValue = dest[removePath]
            if (incomingValue) {
                log.info "\t\tRemove path ($removePath) with existing value ($incomingValue)..."
                dest.remove(removePath)
            } else {
                log.info "\t\t no incomingValue to remove for path ($removePath), nothing to do..."
            }
        }
        return dest
    }


    /**
     * helper function to do 'live' evaluations from config set
     *
     * todo: look at if 'objects' actually work here, or is a string representation good (better?) for setting json values...?
     * @param value
     * @return
     */
    static public Object evaluateValue(String value) {
        null
    }

    String currentTimeStamp(Date date = new Date()){
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(date)
    }


    def performCopyRule(def copyRule, def src, def dest) {
        try {
            String srcValue = srcContext.read(srcPath)
            String origDestValue = destContext.read(destPath)
            DocumentContext dcUpdated = destContext.set(destPath, srcValue)
            String newDestValue = destContext.read(destPath)
            log.warn "Untested? do we need to update destinationTemplate...?"
            Map change = [srcPath: srcPath, srcValue: srcValue, destPath: destPath, origDestValue: origDestValue, newDestValue: newDestValue]
            if (srcValue != newDestValue) {
                log.info "Change: $change"
            }
            changes << change
        } catch (PathNotFoundException pnfe) {
            log.warn "Path (src:$srcPath -> dest:$destPath) wasn't found: $pnfe"
            log.debug "this is bad..."
        }

    }
}
