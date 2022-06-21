package com.lucidworks.ps.transform

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import org.apache.log4j.Logger

import java.text.SimpleDateFormat

/**
 * Class to perform transfomations on JSON slurped
 * @see groovy.json.JsonSlurper
 * todo improve class design - consider implementing an interface, or perhaps a base transformer factory to allow abstraction of implementation choice
 * todo should this handle xml sources as well, or keep that functionality distinct? xml nodes are significantly different that JsonSlurper maps/lists
 * <a href='https://github.com/json-path/JsonPath'>Jayway</a> based transformer
 *
 */
class ObjectTransformerJayway {
    Logger log = Logger.getLogger(this.class.name);
    Map sourceMap
    DocumentContext srcContext

    Map destinationMap
    DocumentContext destContext

    Map rules


    ObjectTransformerJayway(Map srcMap, Map destMap, Map rules) {
        log.info "Constructor: (Map srcMap, Map destMap, Map transformConfig, String pathSeparator)..."
        sourceMap = srcMap
        srcContext = JsonPath.parse(sourceMap)

        destinationMap = destMap
        destContext = JsonPath.parse(destinationMap)

        this.rules = rules
    }

    /**
     * process the configuration rules, currently `set` and `copy`
     * @return
     */
    Map<String, List<Map<String, Object>>> transform() {
        Map resultsMap = [:]
        List<Map<String,Object>> myset = setValues()
        resultsMap['set'] = myset
        List<Map<String,Object>> myCopy = copyValues()
        resultsMap['copy'] = myCopy
        return destinationMap
    }


    /**
     * run through 'set' rules and set destination values based on these rules (with the `$` to signal groovy evaluation of the rule value
     * @return list of rule results
     */
    List<Map<String, Object>> setValues() {
        List<Map<String, Object>> changes = []
        def setRules = rules['set']
        setRules.each { String destPath, String value ->
            try {
                String valToSet = evaluateValue(value)
                String origDestValue = destContext.read(destPath)
                DocumentContext dc = destContext.set(destPath, valToSet)
                String newDestValue = destContext.read(destPath)
                Map change = [srcValue: value, destPath: destPath, origDestValue: origDestValue, newDestValue: newDestValue]
                log.info "\t\tSet '$destPath' in destinationMap to  rule value: '$value'  -- returned doc context:$dc"
                changes << change
            } catch (PathNotFoundException pnfe) {
                log.warn "Path wasn't found: $pnfe"
                log.debug "test"
            }
        }
        return changes
    }

    List<Map<String, Object>> copyValues() {
        List<Map<String, Object>> changes = []

        def copyRules = rules['copy']
        copyRules.each { String destPath, String srcPath ->
            try {
                String srcValue = srcContext.read(srcPath)
                String origDestValue = destContext.read(destPath)
                DocumentContext dcUpdated = destContext.set(destPath, srcValue)
                String newDestValue = destContext.read(destPath)
                Map change = [srcPath: srcPath, srcValue: srcValue, destPath: destPath, origDestValue: origDestValue, newDestValue: newDestValue]
                if(srcValue!=newDestValue){
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

    /**
     * helper function to do 'live' evaluations from config set
     *
     * todo: look at if 'objects' actually work here, or is a string representation good (better?) for setting json values...?
     * @param value
     * @return
     */
    public Object evaluateValue(String value) {
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
