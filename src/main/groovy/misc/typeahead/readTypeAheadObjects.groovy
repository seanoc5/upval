package misc.typeahead

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.clients.FusionResponseWrapper
import com.lucidworks.ps.config.DeployArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonOutput
import org.apache.log4j.Logger
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   8/4/22, Thursday
 * @description: read the typeahead config file,get various fusion objects for packaging
 *
 */

final Logger log = Logger.getLogger(this.class.name)
log.info "Starting ${this.class.name} with args: ${args.findAll { !it.startsWith('-p') }}..."

// ------------------ Configuration -------------------
OptionAccessor options = DeployArgParser.parse(this.class.name, args)
File cfgFile = options.config
//File cfgFile = new File(options.config)
ConfigObject config = null
ConfigSlurper configSlurper = new ConfigSlurper()
Map cliOptionsOverride = [userName:options.user, password:options.password, appName:options.appName]
configSlurper.setBinding(cliOptionsOverride )
if (cfgFile.exists()) {
    log.info "Reading config file: ${cfgFile.absolutePath}"
    config = configSlurper.parse(cfgFile.toURI().toURL())
} else {
    String msg = "Can't find config file: ${cfgFile.absolutePath}!! throwing error..."
    log.warn msg
    throw new IllegalArgumentException(msg)
}


// ------------------ Fusion Client -------------------
FusionClient fusionClient
if(config.fusionClient) {
    log.warn "Trying to use client from configfile: $cfgFile... incomplete code & logic...???"
    fusionClient = config.fusionClient
} else {
    fusionClient = new FusionClient(options)
}
String appID = 'Components'         // note: app name == Component_Packages, but we need to call appid
//def qryPipelines = fusionClient.getQueryPipelines(appID)
String qrypName = 'Components_TYPEAHEAD_DW_QPL_v4'
FusionResponseWrapper responseWrapper = fusionClient.getQueryPipeline(qrypName, appID)
Map qryp = responseWrapper.parsedMap
log.info "Query Pipeline keys: ${qryp.keySet()}"

Map pkgMap = [objects: config.objects, metadata: config.metadata, properties:config.properties]
String json = JsonOutput.prettyPrint(JsonOutput.toJson(config.objects))
log.info "Objects: \n$json"



log.info "Done...?"
