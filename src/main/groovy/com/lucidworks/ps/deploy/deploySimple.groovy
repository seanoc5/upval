package com.lucidworks.ps.deploy

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.config.DeployArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonOutput
import org.apache.log4j.Logger
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   8/4/22, Thursday
 * @description: read the simple config file, get various fusion objects for packaging
 */

final Logger log = Logger.getLogger(this.class.name)
log.info "Starting ${this.class.name} with args: ${args.findAll { !it.startsWith('-p') }}..."

// ------------------ Configuration -------------------
OptionAccessor options = DeployArgParser.parse(this.class.name, args)
File cfgFile = options.config
ConfigObject config = null
ConfigSlurper configSlurper = new ConfigSlurper()
Map cliOptionsOverride = [userName:options.user, password:options.password, appName:options.appName]
configSlurper.setBinding(cliOptionsOverride )
log.info "Reading config file: ${cfgFile.absolutePath}"
config = configSlurper.parse(cfgFile.toURI().toURL())


// ------------------ Fusion Client -------------------
FusionClient fusionClient
if(config.fusionClient) {
    log.warn "Trying to use client from configfile: $cfgFile... incomplete code & logic...???"
    fusionClient = config.fusionClient
} else {
    fusionClient = new FusionClient(options)
}

Map objectsJson = config.objectsJson

String json = JsonOutput.prettyPrint(JsonOutput.toJson(objectsJson))
log.debug "Objects: \n$json"



log.info "Done...?"
