package com.lucidworks.ps.transform

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.internal.JsonContext

import java.util.regex.Pattern

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/28/22, Tuesday
 * @description:
 */

class JsonObjectTransformer extends BaseTransformer {

    /**
     * Construct transformer with a JsonSlurped object (Maps/Collections/LeafNodes), to enable get/set/copy/remove functionality
     * @param source
     * @param destination
     * @param pathSeparator
     */
    JsonObjectTransformer(Object source, Map<String, Object> destination = null, String pathSeparator = '/') {
        this.sourceObject = source
        if (destination) {
            destinationObject = destination
        } else {
            // todo -- refactoring to force destination map, can probably remove this check at some point...?
            destinationObject = source
            log.warn "No destination/template object given in constructor, using source as destination (i.e. inplace updating)"
        }

//        if (checkSourceDestinationTypesAreValid(source, destination)) {
            this.separator = separator
            // get the flattened paths of the source and dest json objects, for the transform to use below
            srcFlatpaths = JsonObject.flattenWithLeafObject(source)
//            JsonObject srcJsonObject = new JsonObject(source)
//            srcFlatpaths = srcJsonObject.flattenWithObjects()

//            def ksFoo = destination.keySet()
            // trouble with lazymaps? inconsistent issues with empty/null, trying this to see about waking up the lazy map...
//            JsonObject destJsonObject = new JsonObject(destination)
//            destFlatpaths = destJsonObject.flattenWithObjects()
            destFlatpaths = JsonObject.flattenWithLeafObject(destinationObject)

//        } else {
//            String msg = "Source and destination are not both valid, throwing error"
//            log.warn msg
//            throw new IllegalArgumentException(msg)
//        }
    }

    /**
     * walk through all flattened entries, and look at the
     * @param pattern
     * @param flatpathItems
     * @return submap of matching flatpath entries
     *
     * todo -- consider returning match object??? premature optimization? just match regexes here, and potential subsequent transform action. (the latter is the current approach)
     */
    Map<String, Object> findAllItemsMatching(String pathPattern, def valuePattern, Map<String, Object> flatpathItems) {
        Map<String, Object> matchingFlatPaths = null
        def keys = flatpathItems.keySet()

        def matchingPaths
        if (pathPattern == '.*' || !pathPattern || pathPattern == '*') {
            log.debug "\t\tShortcut: setting matching paths to all flat paths based on source pathPattern:$pathPattern (?'.*' or empty??)"
            matchingPaths = flatpathItems
        } else {
            matchingPaths = flatpathItems.findAll { String path, Object val ->
                path ==~ pathPattern
            }
            log.debug "\t\tpathPattern($pathPattern) matched: $matchingPaths"
        }

        if (valuePattern) {
            log.debug "\t\tWe have a valuePattern to further filter: $valuePattern"

            matchingFlatPaths = flatpathItems.subMap(matchingPaths.keySet()).findAll { String itemPath, def itemValue ->
                boolean valMatches = false
                if (valuePattern instanceof Pattern) {
                    def valMatch = (itemValue =~ valuePattern)
                    if (valMatch.matches()) {
                        log.debug "Add matcher to return thingie here? not unless things are slow with re-matching in doing the actual rule..."
                        valMatches = true
                    }
                } else {
                    valMatches = ((String) itemValue).contains(valuePattern)
                }
                return valMatches
            }
            log.debug "\t\tpathPattern:$pathPattern :: valuePattern: $valuePattern => Filtered ${matchingPaths.size()} matching paths to ${matchingFlatPaths.size()} matches by value matching, matches: $matchingFlatPaths"

        } else {
            log.debug "No value pattern to match, so return subset of all matched paths"
            Set matchingPathKeys = matchingPaths.keySet()
            matchingFlatPaths = flatpathItems.subMap(matchingPathKeys)
            assert matchingFlatPaths.size() == matchingPaths.size()
            log.debug "\t\tNo valuePattern given, we will return just the path matches..."
        }

        return matchingFlatPaths
    }


    /**
     * get a group of copy rules, iterate through them
     * NOTE: thinking through structure of copy rule:
     * @param copyRules
     * sample rule structure:
     *      [sourcePath:'(.*Leaf)', sourceItemPattern:'(simple)(.*)',
     *       destinationPath:'', destinationExpression:'Complex $1']
     * @return results??
     */
    @Override
    List<Map<String, Object>> performCopyRules(List copyRules) {
        log.info "Rules: $copyRules"
        List<Map> results = []
        copyRules.each { def copyRule ->
            String srcPath = copyRule.sourcePath ?: '.*'
            def srcValPattern = copyRule.sourceItemPattern
            if (!srcValPattern && copyRule.from) {
                log.debug "found alternate src value setting (shortcut label) 'from': ${copyRule.from}"
                srcValPattern = copyRule.from
            }
            def destPath = copyRule.destinationPath
            def destValuePattern = copyRule.destinationExpression ?: copyRule.to        //shortened elvis operator to allow friendly "from->to" labelling

            log.info "\t\tCOPY rule: src path: (${srcPath}) into destination entry:(${destPath ?: 'clone of source'} -- transform: ${srcValPattern ?: 'none'}"
            Map<String, Object> srcPaths = findAllItemsMatching(srcPath, srcValPattern, srcFlatpaths)
            log.info "\t\tfound ${srcPaths.size()} source paths out of (${srcFlatpaths.size()}) matching pattern: $srcValPattern" + srcPaths.collect{"\n\t\t${it.key}"}

            srcPaths.each { String flatPath, def srcValue ->
                Map<String, Object> destPaths = null
                if (destPath) {
                    // rule states we need to copy to destinations different than source, get those destintions
                    destPaths = getDestinationPaths(destPath, flatPath, destValuePattern)
                } else {
                    log.debug "no destination path specified, so just copy to the same dest path as source flatpath: $flatPath"
                    destPaths = ((Map) destFlatpaths).subMap(flatPath)
                }
                def destValue = transformDestinationValue(srcValue, srcValPattern, destPath, destValuePattern)
                if (destValue == srcValue) {
                    log.debug "\t\t$flatPath) destination value and source value are the same: $srcValue"
                } else {
//                    log.debug "\t\ttransformDestinationValue yielded a destination($destValue) different from source:($srcValue)"
                    log.debug "\t\tdo copy source path:($flatPath) with value($srcValue) to destination paths($destPaths) and dest value: $destValue "
                }
                destPaths.each { String dpath, Object origValue ->
                    def result = doCopy(destValue, dpath)
                    log.info "\t\tresult:$result  <---- (original value: $origValue)"
                    results << result
                }
            }
            log.debug "Dest object after copyrules: $destinationObject"
        }
        return results
    }

    /**
     * take the source value (possibly with sourcePattern and destPattern), and crceate the output value
     * @param srcValue
     */
    def transformDestinationValue(def srcValue, def srcPattern, def destPath, def destValuePattern) {
        String destValue = null
        String transformType = getTransformType(srcValue, srcPattern, destPath, destValuePattern)
        switch (transformType) {
//            case '':
            case TX_STRAIGHT_COPY:
                destValue = srcValue
                log.info "\t\t$transformType) transform source: ($srcValue) to dest:($destValue) "
                break

            case TX_REGEX_REPLACE:
                log.debug "\t\t$transformType) transform source:[$srcValue] to dest:[$destValue] with destPattern:[$destValuePattern]"
                destValue = ((String) srcValue).replaceAll(srcPattern, destValuePattern)
                if (destValue == srcValue) {
                    log.info "Dest value:[$destValue] is the same/unchanged from srcValue:[$srcValue] using srcPattern:[$srcPattern] and destPattern:[$destValuePattern] -- is this a problem?"
                } else {
                    log.info "\t\tDest value:[$destValue] is transformed from srcValue:[$srcValue] using srcPattern:[$srcPattern] and destPattern:[$destValuePattern]"
                }
                break

            case TX_TEMPLATE:
                throw new IllegalArgumentException("Incomplete code, we have not implemented Template replace yet...")
                break

            case TX_STRING_REPLACE:
                // todo -- should this be different functionality from regex replace??
                String quoted = Pattern.quote(srcPattern)
                if (quoted != srcPattern) {
                    log.debug "\t\t\t\tEscaping/quoting srcPattern:($srcPattern) => $quoted"
                    destValue = ((String) srcValue).replaceAll(quoted, destValuePattern)
                } else {
                    destValue = ((String) srcValue).replaceAll(srcPattern, destValuePattern)
                }
                log.info "\t\t$transformType) transform source: ($srcValue) to dest:($destValue) "
                break

//            default:
//                destValue = transformWithStringTemplate(srcValue, srcPattern, destPath, destPattern)
        }

        return destValue
    }


    /**
     * placeholder code to consider using string templates
     * @param srcValue
     * @param srcPattern
     * @param destPath
     * @param destPattern
     * @return
     * TODO-- implement/complete this functionality
     */
    String transformWithStringTemplate(def srcValue, def srcPattern, def destPath, def destPattern) {
        String msg = "String template replace/transform Not implemented yet!! Throwing error and running away...  Improper Attempt to transform source: ($srcValue) to dest:($destValue) with destPattern:($destPattern)"
        log.error msg
        throw new IllegalArgumentException(msg)

        // todo -- look at String templating: https://docs.groovy-lang.org/docs/next/html/documentation/template-engines.html
        StringBuilder valToSet = new StringBuilder()
        log.info "We have a destination value (assume it is a transform pattern...: $destValuePattern"
        def valMatch = (value =~ srcValPattern)
        if (valMatch.matches()) {
            def groups = valMatch[0]
            if (groups.size() > 1) {
                log.warn "More code here: stringbuilder for destination pattern..."
                int i = 1
                groups[1..-1].each {
                    log.info "\t\tReplace $i: " + groups[i]
                    i++
                }
                JsonObject.setObjectNodeValue(destinationObject, srcValue + "--should have been modified....")
            } else {
                log.info "found a match but no groups....?"
            }
            log.info "\t\tbuild destination value, matcher: ${[0]}"
        } else {
            log.warn "We have a destination value, but no match... what do we do? panic?? source path: [$flatPath] srcval: $value :: destValPattern: $destValuePattern"
        }
        //                        } else {
        //                            log.debug "\t\tNo destValuePattern so just straight value ($srcValue) copy from srcpath($flatPath) to dest object"
        //                            JsonObject.setObjectNodeValue(destinationObject, flatPath, srcValue)
        //                            log.debug "Dest object map: ${destinationObject.keySet()}"
        //                        }
    }


    public static final String TX_STRAIGHT_COPY = 'straightCopy'
    public static final String TX_STRING_REPLACE = 'stringReplace'
    public static final String TX_REGEX_REPLACE = 'regexReplace'
    public static final String TX_TEMPLATE = 'template'

    String getTransformType(def srcValue, def srcPattern, def destPath, def destPattern) {
        String txType = null
        if (srcPattern) {
            if (destPattern) {
                if (srcPattern instanceof Pattern) {
                    log.debug "\t\tsrcPattern($srcPattern) is a Pattern, so do TX_REGEX_REPLACE"
                    txType = TX_REGEX_REPLACE
                } else if (destPattern instanceof Pattern) {
                    log.debug "\t\tdestPattern($destPattern) is a Pattern, so do TX_REGEX_REPLACE"
                    txType = TX_REGEX_REPLACE           // String Template here??
                } else {
                    txType = TX_STRING_REPLACE
                    log.debug "both source($srcPattern) and dest($destPattern) are strings, so this will be a simple string replace"
//                    log.warn "Unknown Transaction type, defaulting to ($txType) but we have both source and destination patterns, so this is probably incorrect!! "
                }

            } else {
                log.info "\t\tSource pattern but no dest pattern, assuming straight copy (with source pattern filtering)"
                txType = TX_STRAIGHT_COPY
            }

        } else {
            log.info "No source or dest pattern, just perform a simple value copy"
            txType = TX_STRAIGHT_COPY
        }
        log.debug "\t\tTX type ($txType) from SourcePattern:(${srcPattern}) DestPattern: ${destPattern}..."
        return txType
    }

    @Override
    List<Map<String, Object>> performSetRules(List rules) {
        List results = []
        rules.each { Map rule ->
            log.info "\t\tset rule: $rule"
            def destPath = rule.destinationPath ?: rule.path
            def valToSet = rule.destinationExpression ?: rule.to        //shortened elvis operator to allow friendly "from->to" labelling

            def result = JsonObject.setObjectNodeValue(destinationObject, destPath, valToSet)
            results << rule
        }
        return results
    }


//    static public final int charPreA = 64
    /**
     * remove nodes based on rules
     * @param removeRules
     * @return results of removal (list of removed nodes??)
     * Note: we assume JsonObject.orderIndexKeysDecreasing is necessary to void trying to remove successive collection elements, and have collection indexes change in the process (if we work highest index to lowest, are we safe???)
     */
    @Override
    List<Map<String, Object>> performRemoveRules(List removeRules) {
        List results = []
        // Jayway syntax (probably default, no need for map style delete or set)
        JsonContext destContext = JsonPath.parse(destinationObject)
        // home-grown map-style rule operations (perhaps only useful for copy/transform)
        def destObject = new JsonObject(this.destinationObject)

        // todo -- revisit/refactor, this is a quick hack to get 'full' map, including non-leaf nodes....
        removeRules.each { def rule ->
            if(rule instanceof Map) {
                log.info "Remove rule (map-style): $rule"
                // these pattern variables can be String or Regex Pattern
                def pathPattern = rule.pathPattern
                def valuePattern = rule.valuePattern
                Map<String, Object> matchingPaths = null
                matchingPaths = destObject.findItems(pathPattern, valuePattern)
                // matchingPaths = findAllItemsMatching(pathPattern, valuePattern, this.destFlatpaths)
                // todo -- revisit removal logic and any missed gothca's in removing things, especially collection elements
                Set sortedKeys = JsonObject.orderIndexKeysDecreasing(matchingPaths)
                sortedKeys.each { String path ->
                    def rslt = doRemove(path)
                    results << rslt
                }
            } else if(rule instanceof String && (String)rule.startsWith('$')){
                def preDelete = destContext.read(rule)
                Map result = [:]
                if(preDelete) {
                    log.info "Remove rule (jayway syntax): $rule -- matches before delete: $preDelete"
                    def postDeleteContext = destContext.delete(rule)
                    result = [jaywayPath: rule, predelete:preDelete]
                } else {
                    log.info "Remove rule (jayway syntax): $rule -- NO MATCHES -- no delete"
                    result = [jaywayPath: rule, status:'nothing to delete']
                }
                results << result
            } else {
                String msg = "Unknown rule type/syntax: $rule  (not UpVal Map-based, not JayWay...???)"
                log.warn msg
                results << [jaywayPath: rule, status:msg]
            }

        }

        return results
    }


    @Override
    Map<String, Object> doCopy(def valToSet, String destPath) {
        log.info "\t\tSet dest node: $destPath to value: ($valToSet)"
        def result = JsonObject.setObjectNodeValue(destinationObject, destPath, valToSet)
        return result
    }

    @Override
    Map<String, Object> doSet(def valToSet, String destNodePath) {
        log.warn "Implement me!! blank operation at the moment"
        return null
    }

    @Override
    Map<String, Object> doRemove(String path) {
        def result = JsonObject.removeItem(path, destinationObject)
        log.info "\t\tRemove destNode by path: ${path} -- result: $result"
        return result
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

    Map<String, Object> getDestinationPaths(def destPath, def srcPath, def destValuePattern = null) {
        Map<String, Object> destPaths = [:]
        if (destPath) {
            // todo -- revisit if we ever have multiple destination paths that do not matchin 1:1 source path...? remove this code??
            destPaths = findAllItemsMatching(destPath, '.*', destFlatpaths)
            log.info "\t\tDestPath ($destPath) -> Destination path(s): $destPaths "
        } else {
            destPaths = findAllItemsMatching(srcPath, '', destFlatpaths)
            log.info "\t\tNo destination path(s), so we will mirror source path ($srcPath) for destination path list: ($destPaths) -- Does this work correctly???"
        }
        return destPaths
    }
}
