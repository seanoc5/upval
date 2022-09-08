package misc.typeahead

import com.lucidworks.ps.clients.FusionClient
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

Map objectsJson = config.objectsJson

def props = config.props
Map pkgMap = [objects: objectsJson, metadata: objectsJson.metadata, properties:objectsJson.properties]
pkgMap.each { key, val ->
    if (val instanceof Map) {
        log.info "\t\t$key) map with keys: ${val.keySet()}"
    } else if (val instanceof List) {
        log.info "\t\t$key) list with size: ${val.size()}"
    }
}
String json = JsonOutput.prettyPrint(JsonOutput.toJson(config.objects))
log.debug "Objects: \n$json"



log.info "Done...?"
