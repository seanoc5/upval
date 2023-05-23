package misc

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.PathNotFoundException
import com.jayway.jsonpath.internal.JsonContext
import net.minidev.json.JSONArray
import org.apache.log4j.Logger

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Use <a href='https://github.com/json-path/JsonPath'>Jayway</a> based transformer to apply rules to a JsonSlurper.parse* object (i.e. map of maps/lists)
 * todo -- handle an initial list rather than the assumed map (with possible lists as children)
 * Class to perform transfomations on JSON slurped
 * @see groovy.json.JsonSlurper* todo improve class design - consider implementing an interface, or perhaps a base transformer factory to allow abstraction of implementation choice
 * todo should this handle xml sources as well, or keep that functionality distinct? xml nodes are significantly different that JsonSlurper maps/lists
 * <a href='https://github.com/json-path/JsonPath'>Jayway</a> based transformer
 *
 */
class ObjectTransformerJayway {
    static Logger log = Logger.getLogger(this.class.name);
    public static final String ALL = 'ALL'
    Map sourceMap
    Configuration pathsConf = Configuration.builder().options(Option.AS_PATH_LIST).build();
    JsonContext jsonPathsContext = null
    JsonContext srcContext = null
    JSONArray allJsonPaths = null
    public static final String ALL_PATH = '$..*'
    // convenience constant for Jayway "give me everything" search path

    ObjectTransformerJayway(Map srcMap) {
        log.debug "Constructor: (Map srcMap, Map destMap, Map transformConfig, String pathSeparator)..."
        sourceMap = srcMap

        srcContext = JsonPath.parse(sourceMap)
        jsonPathsContext = JsonPath.using(pathsConf).parse(sourceMap)
        allJsonPaths = jsonPathsContext.read(ALL_PATH)
    }


    /**
     * Get a list of
     * @param matchPath
     * @param matchValue
     * @return
     */
    List<String> getPaths(def matchPath = ALL, def matchValue = ALL) {
        log.info "Find paths matching path:($matchPath) and/or value: ($matchValue)..."

        List<String> paths = []
        if (matchPath == ALL) {
            paths = allJsonPaths
            log.info "Matching all json paths... ${paths.size()} total count"
        } else {
            paths = getPathMatches(matchPath)
            log.info "\t\tPaths matching path ($matchPath): $paths"
        }
        List<String> valPaths = []
        if (matchPath == ALL) {
            valPaths = allJsonPaths
            log.info "Matching all path values... ${paths.size()} total count"

        } else {
            valPaths = getPathsByValue(matchValue)
            log.info "\t\tPaths matching value ($matchValue): $valPaths"
        }

        List<String> results = paths.intersect(valPaths)
        log.info "\t\t getPaths($matchPath, $matchValue) results: $results"

        return results
    }

    /**
     * Search the expanded list of 'ALL' paths, and check if the (String) VALUE matches
     * NOTE: assumes string value matches only, as the main goal is for variable substitution, which is mostly names
     * @param matchValue
     * @return list of matching paths (to do work on: set, copy, delete,...)
     */
    List<String> getPathsByValue(def matchValue) {
        List matches = []
        allJsonPaths.each { String path ->
            boolean leafNode = true
            def val = srcContext.read(path)         // todo -- only dealing with Strings at the moment...
            if (val instanceof String) {
                log.debug "String value: $val"
            } else if (val instanceof Boolean) {
                log.debug "Boolean value: $val"
            } else if (val instanceof Number) {
                log.debug "Number value: $val"
            } else if (val instanceof Map) {
                leafNode = false
                log.debug "\t\tMap (skip getPathsByValue): $val"
            } else if (val instanceof Collection) {
                leafNode = false
                log.debug "\t\tCollection (skip getPathsByValue): $val"
            } else {
                log.warn "Not string or number: ${val.class.simpleName} :: $val"
            }
            if (leafNode) {
                if (matchValue instanceof String) {
                    if (val?.toString().contains(matchValue)) {
                        matches << path
                        log.info "Path($path) match (String contains: $matchValue) -- value:${val}"
                    }
                } else if (matchValue instanceof Pattern) {
                    if (val =~ matchValue) {        // todo -- cast to string?
                        matches << path
                        log.info "Path($path) match (REGEX Pattern: $matchValue) -- value:${val}"
                    }
                } else {
                    log.info "\t\tMatchValue($matchValue) is not a string NOR a pattern, but rather: (${matchValue.class.name}). No idea what to do with that..."
                }
            } else {
                log.debug "Skipping non leafnode: $path"
            }
        }
        return matches
    }


    /**
     * Search the expanded list of 'ALL' paths, and check if the (String) PATH matches
     * NOTE: assumes string value matches only, as the main goal is for variable substitution, which is mostly names
     * @param matchValue String or regex Pattern to match path segments on
     * @return list of matching paths (to do work on: set, copy, delete,...)
     * Note: returning list of paths is bracket format rather than dot-notation:
     * e.g. "$.properties.collection" -> "$['properties']['collections']", this treats the whole path as a single string,
     * so if you want to cross path segments, be aware of the quotes and brackets
     */
    List<String> getPathMatches(def matchValue) {
        List matches = null
        if (matchValue instanceof String) {
            matches = allJsonPaths.findAll { String path -> path.contains(matchValue) }
        } else if (matchValue instanceof Pattern) {
            matches = allJsonPaths.findAll { it =~ matchValue }
        } else {
            throw new IllegalArgumentException("Match value ($matchValue) was not a String or Pattern, it was: ${matchValue.class.name} -- bailing!!")
        }
        return matches
    }


    /**
     * convenience wrapper around jayway, potentially useful for future improvements or extensions
     * @param jaywayPath
     * @return (String?) value returned from jayway path
     */
    def read(String jaywayPath) {
        def val = srcContext.read(jaywayPath)
        log.debug "Read from jayway path ($jaywayPath) and got value: ($val)"
        return val
    }


    /**
     * convenience method to return map of path->valu
     * @param paths
     * @return map of path->value
     */
    Map<String, Object> read(List<String> paths) {
        Map m = paths.collectEntries { String path ->
            def val = srcContext.read(path)
            log.info "\t\t$path -> $val"
            [(path): val]
        }
        return m
    }


    /**
     * method to make a change to the value at a path (as opposed to renamePath()).
     * @param originalValue String value for source of update
     * @param from String or Pattern to indicate what part(s) of the source should be involved (especially for regexes and capture groups)
     * @param to 'instructions' on how to create the updated value
     */
    def update(String originalValue, def from, def to) {
        log.info "Original value: '$originalValue' :: from:($from) -> to:($to)"
        def newValue
        if (from instanceof Pattern) {
            Matcher matcher = ((Pattern) from).matcher(originalValue)
            if (matcher.matches()) {
                log.info "\t\tMatch! matcher: $matcher"
            } else {
                log.warn "No match: $matcher"
            }
        } else if (from instanceof String) {
            newValue = originalValue.replaceAll(from, to)
            log.info "New Value: $newValue"
        } else {
            log.warn "'from' was not a string nor a pattern, what do we do???"
        }
    }

    /**
     * Static (functional-friendly?) method to apply transform rules
     * @param rulesMap map of elements (can contain list children, yes?) to transform, with optional destination template (for default 'new' values)
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
    static Map<String, List<Map<String, Object>>> transform(Map rulesMap, Map rules, Map destinationTemplate = [:]) {
        Map resultsMap = destinationTemplate

        // ---------- COPY -------------
        List<Map<String, Object>> myCopy = copyValues(rulesMap, rules)
        resultsMap = resultsMap + myCopy

        // ---------- SET -------------
        List<Map<String, Object>> myset = setValues(rulesMap, rules)
        resultsMap = resultsMap + myset

        // ---------- REMOVE -------------
        if (rules.remove) {
            log.warn "Untested code/functionality at the moment... write tests, fix broken stuff... remove rules: ${rules.remove}"
        }
        List<Map<String, Object>> myRemove = removeItems(rulesMap, rules, destinationTemplate)


        return resultsMap

    }


    /**
     * run through 'set' rules and set destination values based on these rules
     * <br>(with the `$` to signal groovy evaluation of the rule value)
     * @return list of rule results
     */
    static List<Map<String, Object>> setValues(Map srcMap, Map rules, Map destinationTemplate = [:]) {
//    static List<Map<String, Object>> setValues(Map srcMap, Map rules, Map destinationTemplate = [:]) {
        List<Map<String, Object>> changes = []
        def setRules = rules['set']
        DocumentContext srcContext = JsonPath.parse(srcMap)
        DocumentContext destContext = JsonPath.parse(destinationTemplate)

        setRules.each { String destPath, String value ->
            String valToSet = evaluateValue(value)
            String origDestValue = null
            try {
                origDestValue = srcContext.read(destPath)
                log.info "\t\toverriding orignal dest value ($origDestValue) with set value ($valToSet)"
            } catch (PathNotFoundException pnfe) {
                log.warn "Path wasn't found: $pnfe"
            }

            // make the change here...
            DocumentContext dc = srcContext.set(destPath, valToSet)
            // confirm the updated value here (optional??)
            String newDestValue = srcContext.read(destPath)
            Map change = [srcValue: value, destPath: destPath, origDestValue: origDestValue, newDestValue: newDestValue]
            log.info "\t\tSet '$destPath' in destinationMap to  rule value: '$value'  -- returned doc context:$dc"
            changes << change
        }
        return changes
    }


    /**
     * update the srcMap (from the constructor) with update rules
     * @param updateRules list of Map of rules for updating (optional path, value to match, and value-or-variable to set)
     * @return changes made (for review)
     */
    List<Map<String, Object>> updateSourceValues(List<Map<String, Object>> updateRules) {
        List<Map<String, Object>> changes = []
        log.info "\t\tUpdate rules: $updateRules"

        updateRules.eachWithIndex { Map rule, int idx ->
            log.info "\t\t$idx) Rule: ${rule}"
            performUpdateRule(rule)
            log.info "\t\tRule: $rule"
//            changes << performCopyRule(rule, srcMap, destinationTemplate)
        }
        return changes
    }


    def performUpdateRule(Map rule) {
//        if(rule.
        log.info "Perform rule: $rule -- todo: more code"
    }


    /**
     * Replace (in-placee) values with Fusion import-object variables
     * <br><b>NOTE: this will modify the srcMap</b>
     * <br> i.e. for typeahead packaging 'Component_' in an id should become '${ta.APPNAME}'
     * <br> then a variables.json file can devine ta.APPNAME andd the import process will provide a dialog for the importer to accept defaults, or set values
     * @param variables a map of variable name -> transform map.
     * <br> transformMap will have 'from' (for matching value in leaf nodes), or 'rename' to change a path name,
     * <br> and 'default' is optional, which will become the 'default' value in the exported variables.json
     * @return list of variable names and default values, intended to write to a file to match up with a zip file for importing
     */
    Map<String, String> performVariableSubstitution(Map<String, Map<String, String>> variables) {
        Map<String, String> outputVariables = [:]
        variables.each { String varName, Map<String, String> transformMap ->
            String matchValue = transformMap.from     // optional filter of matching by value (should have either path from, but both are fine as well)
//            String matchPath = transformMap.path     // optional filter of matching by path
            String defaultValue = transformMap.default ?: 'replaceme'

            // todo -- fixme -- some problems here, need to revisit - no destFlatpaths defined...
            def matches = this.findAllItemsMatching('.*', matchValue, destFlatpaths)
            log.info "\t\tVAR SUBS: from:$matchValue -> to (varName):$varName :: var default value: $defaultValue -- matches: $matches"
            matches.each { String path, Object foo ->
                log.info "\t\tsubstitute: $path ($foo) -> from:($matchValue) => varname: $varName"
                def postSet = doSet(varName, path)
                log.debug "post set: $postSet"
            }
            log.info "var:$varName) transform:$transformMap"
            outputVariables.put(varName, defaultValue)
        }
        return outputVariables
    }


    /**
     * Copy values from source to destination with an optional destinationTemplate holding defaults
     * @param srcMap source values to copy from
     * @param rules to dictate what to copy (from source to destinationTemplate (same path -- s)
     * @param destinationTemplate
     * @return change list (destinationTemplate is created/updated as we go...?
     */
    static List<Map<String, Object>> copyValues(Map srcMap, Map rules) {
//    static List<Map<String, Object>> copyValues(Map srcMap, Map rules, Map destinationTemplate = [:]) {
        List<Map<String, Object>> changes = []
        DocumentContext srcContext = JsonPath.parse(srcMap)
//        DocumentContext destContext = JsonPath.parse(destinationTemplate)

        def copyRules = rules['copy']
        log.info "\t\tCopy rules: $copyRules"

        copyRules.each { Map.Entry rule ->
            log.info "Rule class: ${rule.getClass().simpleName}"
            performCopyRule(rule, srcMap, [:], srcContext)
            log.info "\t\tRule: $rule"
//            changes << performCopyRule(rule, srcMap, destinationTemplate)
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

    String currentTimeStamp(Date date = new Date()) {
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(date)
    }

//    static Map performCopyRule(Map.Entry copyRule, def src, def dest, DocumentContext srcContext) {
    static Map performCopyRule(Map.Entry copyRule, def src, def dest, DocumentContext srcContext, DocumentContext destContext) {
        Map change = [:]
        String srcPath = copyRule.key
        String destPath = copyRule.value
        try {
            String srcValue = srcContext.read(srcPath)
            String origDestValue = destContext.read(destPath)
            DocumentContext dcUpdated = destContext.set(destPath, srcValue)
            String newDestValue = destContext.read(destPath)
            log.warn "Untested? do we need to update destinationTemplate...?"
            change = [srcPath: srcPath, srcValue: srcValue, destPath: destPath, origDestValue: origDestValue, newDestValue: newDestValue]
            if (srcValue != newDestValue) {
                log.info "Change: $change"
            }
        } catch (PathNotFoundException pnfe) {
            log.warn "Path (src:$srcPath -> dest:$destPath) wasn't found: $pnfe"
            log.debug "this is bad..."
        }
        return change
    }

//    Map sourceMap
//    DocumentContext srcContext

//    Map destinationMap
//    DocumentContext destContext

//    Map rules

    /**
     * todo -- working on moving to functional approach (static calls)
     */
//    ObjectTransformerJayway(Map srcMap, Map destMap, Map rules) {
//        log.debug "Constructor: (Map srcMap, Map destMap, Map transformConfig, String pathSeparator)..."
//        sourceMap = srcMap
//        destinationMap = destMap
//
//        srcContext = JsonPath.parse(sourceMap)
//        destContext = JsonPath.parse(destinationMap)
//
//        this.rules = rules
//    }

    /**
     * process the configuration rules, currently `set` and `copy`
     * @return
     * todo -- remove me, moving to functional-friendly approach (static calls)
     */
//    Map<String, List<Map<String, Object>>> transform() {
//        Map resultsMap = [:]
//        List<Map<String, Object>> myset = setValues(sourceMap, rules, destinationMap)
//        resultsMap['set'] = myset
//        List<Map<String, Object>> myCopy = copyValues(sourceMap, rules, destinationMap)
//        resultsMap['copy'] = myCopy
//        return destinationMap
//    }

}
