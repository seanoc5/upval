package misc.experiment

import groovy.json.JsonSlurper
import org.apache.log4j.Logger

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   8/9/22, Tuesday
 * @description:
 */

/** experimenting with writing config files (not complete) */

Logger log = Logger.getLogger(this.class.name);

JsonSlurper slurper = new JsonSlurper()
File srcJson = new File('/home/sean/work/lucidworks/upval/src/main/resources/typeahead/Features_TYPEAHEAD_DW_F5.4_v4_Feature_TYPEAHEAD_DW_F5.4_v4_objects.json')
//URL url = getClass().getResource('/resources/typeahead/Features_TYPEAHEAD_DW_F5.4_v4_Feature_TYPEAHEAD_DW_F5.4_v4_objects.json')
Map  json = slurper.parse(srcJson)

ConfigObject config  = new ConfigSlurper().parse('test {foo = "bar"}')
//config.putAll(json.objects.queryPipelines)


json.objects.indexPipelines.each {
    config.put('indexPipeline', it)
}

File outConfig = new File(srcJson.parentFile, 'configTaObjects.groovy')
outConfig.withWriter {
    config.writeTo(it)
}

log.info "Pretty print: ${config.prettyPrint()}"
