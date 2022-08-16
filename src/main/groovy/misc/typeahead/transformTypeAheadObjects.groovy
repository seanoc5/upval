package misc.typeahead

import com.lucidworks.ps.Helper
import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.clients.TransformArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.transform.Field
import org.apache.log4j.Logger
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   8/4/22, Thursday
 * @description:
 */
@Field
final Logger log = Logger.getLogger(this.class.name)
log.info "Starting ${this.class.name} with args: ${args}..."


// ------------------ Configuration -------------------
OptionAccessor options = TransformArgParser.parse(this.class.name, args)
URL configUrl = getClass().getResource(options.config)
String appName = options.appName
String taName = options.taName
ConfigSlurper configSlurper = new ConfigSlurper()
Map bindingMap = [appName: appName]
if (taName) {
    bindingMap['taName'] = taName
    log.info "Found typeahead name from CLI/options: $taName -- set binding to override config file...."
}
configSlurper.setBinding(bindingMap)
ConfigObject config = configSlurper.parse(configUrl)
log.info "ConfigSlurper using: $configUrl"
log.debug "ConfigObject: $config"


// ------------------ Fusion Client -------------------
FusionClient fusionClient
if(options.fusionUrl) {
    fusionClient = new FusionClient(options)
    log.info "Created Fusion client: $fusionClient"
}
// ------------------ Export directory -------------------
def exportDir
if(options.exportDir){
    exportDir = Helper.getOrMakeDirectory(options.exportDir)
    log.info "Using export directory: $exportDir"
}


log.info "Done...?"

