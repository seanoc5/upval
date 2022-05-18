package com.lucidworks.ps.js

import org.apache.log4j.Logger

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/18/22, Wednesday
 * @description:
 */

/**
 * helper class to analyze javascript in index pipeline stages (regular or managed)
 */
class PipelineJsAnalyzer {
    static Logger log = Logger.getLogger(this.class.name);

    /**
     * get all the interesting JS Stages
     * @param allStages
     * @return
     */
    static Map<String, List> collectJsStages(List allStages) {
        Map<String, List> jsCodeStages = [:].withDefault { [] }
        Map<String, List> stageTypes = allStages.groupBy { it.type }
        log.info "Query pipeline stage types:\n" + stageTypes.collect { String key, def val -> "${val.size()} :: $key" }.join('\n')

        // todo -- are there other types that we might be missing?
        // def jsTypes = ['javascript-query', 'managed-js-query']
        stageTypes.each { String type, List stages ->
            if (type.startsWith('javascript') || type.startsWith('managed-js')) {
                stages.each {
                    String label = it.label?.trim()
                    String script = it.script?.trim()
                    String s = "// ----------------$label ($type) --------------\n$script\n\n"
                    if (script) {
                        if(label) {
                            log.info "Added script body '$label' ($type) to collection for analysis (good) "
                        } else {
                            log.warn "No label ($label) for script with size: ${script.size()}"
                        }
                        jsCodeStages[type] << s
                    } else {
                        log.warn "No script in Query stage (label:$label)?? Script:$it (bad)"
                    }
                }
            } else {
                log.debug "Skip stage type: $type (${stages.size()} count)"
            }
        }
        return jsCodeStages
    }

//        jsCodeLines.groupBy {
//            String line = it.trim()
//            switch (line) {
//                case { !line }:
//                    'empty'
//                    break
//                case ~/[{}()]/:
//                    'braces'
//                    break
//                case ~/var \w+ ?=.*/:
//                    'assignment'
//                    break
//                case ~/ /:
//                    ' '
//                    break
//
//                case ~/ /:
//                    ' '
//                    break
//
//                case ~/ /:
//                    ' '
//                    break
//
//                case ~/ /:
//                    ' '
//                    break
//
//                default:
//                    'unknown'
//
//                    log.info "done...?"


}
