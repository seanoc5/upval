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

    static Map<String, List<Map<String, Object>>> transform(Map srcMap, Map rules, Map destinationTemplate = [:]) {
        Map resultsMap = destinationTemplate

        List<Map<String, Object>> myset = setValues(srcMap, rules, destinationTemplate)
        resultsMap = resultsMap + myset
        List<Map<String, Object>> myCopy = copyValues(srcMap, rules, destinationTemplate)
        resultsMap = resultsMap + myCopy
        return resultsMap
    }


    /**
     * run through 'set' rules and set destination values based on these rules (with the `$` to signal groovy evaluation of the rule value
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

            DocumentContext dc = destContext.set(destPath, valToSet)
            String newDestValue = destContext.read(destPath)
            Map change = [srcValue: value, destPath: destPath, origDestValue: origDestValue, newDestValue: newDestValue]
            log.info "\t\tSet '$destPath' in destinationMap to  rule value: '$value'  -- returned doc context:$dc"
            changes << change
        }
        return changes
    }

    static List<Map<String, Object>> copyValues(Map srcMap, Map rules, Map destinationTemplate = [:]) {
        List<Map<String, Object>> changes = []
        DocumentContext srcContext = JsonPath.parse(srcMap)
        DocumentContext destContext = JsonPath.parse(destinationTemplate)

        def copyRules = rules['copy']
        copyRules.each { String destPath, String srcPath ->
            try {
                String srcValue = srcContext.read(srcPath)
                String origDestValue = destContext.read(destPath)
                DocumentContext dcUpdated = destContext.set(destPath, srcValue)
                String newDestValue = destContext.read(destPath)
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
        return changes
    }

    static List<Map<String, Object>> removeItems(Map srcMap, Map rules) {
        List removeRules = rules['remove']
        removeRules.each { String removePath ->
            def incomingValue = srcMap[removePath]
            if (incomingValue) {
                log.info "\t\tRemove path ($removePath)..."
                srcMap.remove(removePath)
            } else {
                log.info "\t\t no incomingValue to remove for path ($removePath), nothing to do..."
            }
        }

    }


    /**
     * helper function to do 'live' evaluations from config set
     *
     * todo: look at if 'objects' actually work here, or is a string representation good (better?) for setting json values...?
     * @param value
     * @return
     */
    static public Object evaluateValue(String value) {
        String valueToSet = null
        if (value.contains('$')) {
            try {
                // 2020-11-05T19:12:54.966Z
                // new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(oldDate)
//                String nowStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:sss'Z'").format(new Date())

                // http://www.groovyconsole.appspot.com/edit/22004?execute
                log.debug "\t\tprepare to evaluate me...: $value"
                valueToSet = Eval.me(/"$value"/)
                log.info "\t\tEvaluated gstring ($value) ==> $valueToSet"
            } catch (Exception e){
                log.warn "Error: $e"
            }
        } else {
            valueToSet = value
        }
        return valueToSet
    }

    String currentTimeStamp(Date date = new Date()){
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(date)
    }
}
