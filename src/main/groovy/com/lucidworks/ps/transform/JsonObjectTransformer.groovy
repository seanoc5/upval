package com.lucidworks.ps.transform

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
    JsonObjectTransformer(Object source, Map<String, Object> destination = [:], String pathSeparator = '/') {
        if (checkSourceDestinationTypesAreValid(source, destination)) {
            this.sourceObject = source
            if (destination) {
                destinationObject = destination
            } else {
                log.warn "No destination/template object given in constructor, clone source object for now (want to have JsonObject be able to create missing hierarchy, but not working at the moment"
                destinationObject = source.clone()
            }

            this.separator = separator
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
     * walk through all flattened entries, and look at the
     * @param pattern
     * @param flatpathItems
     * @return submap of matching flatpath entries
     *
     * todo -- consider returning match object??? premature optimization? just match regexes here, and potential subsequent transform action. (the latter is the current approach)
     */
    def findAllItemsMatching(def pathPattern, def valuePattern, Map<String, Object> flatpathItems) {
        def matches = null
        def keys = flatpathItems.keySet()
        def matchingPaths = keys.findAll { String path ->
            path ==~ pathPattern
        }
        log.info "\t\tpathPattern($pathPattern) matched: $matchingPaths"

        if (valuePattern) {
            log.debug "We have a valuePattern to further file"
            matches = flatpathItems.subMap(matchingPaths).findAll { String key, def val ->
                def valMatch = (val =~ valuePattern)
                if (valMatch.matches()) {
                    log.debug "Add matcher to return thingie here? not unless things are slow with re-matching in doing the actual rule..."
                }
                return valMatch.matches()
            }
            log.debug "\t\tFiltered ${matchingPaths.size()} matching paths to ${matches.size()} matches by value matching..."
        } else {
            matches = matchingPaths
            log.debug "\t\tNo valuePattern given, we will return just the path matches..."
        }
        return matches
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
    def performCopyRules(List<Map> copyRules) {
        log.info "Rules: $copyRules"
        List results = []
        copyRules.each { def copyRule ->
            String srcPath = copyRule.sourcePath
            def srcValPattern = copyRule.sourceItemPattern
            def destPath = copyRule.destinationPath
            def destValuePattern = copyRule.destinationExpression

            log.info "\t\tCOPY rule: src path: (${srcPath}) into destination entry:(${destPath ?: 'same as source'} -- transform: ${srcValPattern ?: 'none'}"
            def srcPaths = findAllItemsMatching(srcPath, srcValPattern, srcFlatpaths)

            def destPaths = getDestinationPaths(destPath)

            srcPaths.each { String flatPath, def srcValue ->
//            srcPaths.each { String flatPath ->
//                def srcValue = srcFlatpaths[flatPath]
                def destValue = transformDestinationValue(srcValue, srcValPattern, destPath, destValuePattern)
                log.info "\t\tdo copy value($srcValue) from source($flatPath) to destmap($destinationObject) ?"
                def result = doCopy(destValue, )
                log.info "do copy here: $it"
            }
            log.debug "Found items matching paths: $srcPaths"
            log.info "Dest object after copyrules: $destinationObject"
            results << copyRule
        }
        return results

    }

    /**
     * take the source value (possibly with sourcePattern and destPattern), and crceate the output value
     * @param srcValue
     */
    def transformDestinationValue(def srcValue, def srcPattern, def destPath, def destPattern) {
        String destValue = null
        String transformType = getTransformType(srcValue, srcPattern, destPath, destPattern)
        switch (transformType) {
            case '':
            case TX_STRAIGHT:
                destValue = srcValue
                break

            case '~':
            case TX_REGEX_REPLACE:
                destValue = ((String) srcValue).replaceAll(srcPattern, destPattern)
                log.info "\t\t$TX_REGEX_REPLACE) transform source: ($srcValue) to dest:($destValue) with destPattern:($destPattern) "
                break

            case TX_TEMPLATE:
            default:
                destValue = transformWithStringTemplate(srcValue, srcPattern, destPath, destPattern)
        }



    }

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


    public static final String TX_STRAIGHT = 'straight'
    public static final String TX_REGEX_REPLACE = 'regexReplace'
    public static final String TX_TEMPLATE = 'template'

    String getTransformType(def srcValue, def srcPattern, def destPath, def destPattern) {
        String txType = null
        if (srcPattern) {
            if (((String) srcPattern).startsWith('~')) {
                txType = TX_REGEX_REPLACE
            } else if (destPattern) {
                if (srcPattern.contains('(') && destPattern.contains('$')) {
                    txType = TX_TEMPLATE
                } else {
                    txType = TX_STRAIGHT
                    log.warn "Unknown Transaction type, defaulting to ($txType) but we have both source and destination patterns, so this is probably incorrect!! "
                }

            } else {
                log.info "\t\tSource pattern but no dest pattern, assuming straight copy (with source pattern filtering)"
                txType = TX_STRAIGHT
            }

        } else {
            txType = TX_STRAIGHT
        }
        log.info "\t\tTX type ($txType) from SourcePattern:(${srcPattern}) DestPattern: ${destPattern}..."
        return txType
    }

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
        rules.each { def rule ->
            log.info "Remove rule: $rule"
            results << rule
        }
        return results

    }

    @Override
    def doCopy(def valToSet, def destNodes) {
        log.info "Set destNode: $destNodes (just using the single/current srcPath for now) to value: $valToSet"
        destNodes.each {def destNode ->
            log.info "\t\tSet dest node: $destNode"
            JsonObject.setObjectNodeValue(destinationObject, destFlatpaths, valToSet)
        }
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

    List getDestinationPaths(def destPath, def srcPath, def destValuePattern = null) {
        List destPaths = []
        if (destPath) {
            log.info "\t\tDestination path(s): $destPath"
            // todo -- revisit if we ever have multiple destination paths that do not matchin 1:1 source path...? remove this code??
            destPaths = findAllItemsMatching(destPath, '.*', destFlatpaths)
        } else {
            destPath = srcPath
            log.info "\t\tNo destination path(s): mirror source path ($srcPath) for destination: ($destPath)"
        }

    }
}
