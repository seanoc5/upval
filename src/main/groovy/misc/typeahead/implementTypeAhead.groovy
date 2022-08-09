package misc.typeahead

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.clients.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

import java.nio.file.Path
import java.nio.file.Paths
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   8/4/22, Thursday
 * @description:
 */
final Logger log = Logger.getLogger(this.class.name);

log.info "Starting ${this.class.name} with args: ${args}..."

OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)
Path cfgUrl = Paths.get(options.config)
File f = new File("/home/sean/work/lucidworks/upval/src/main/resources/configs/configTypeAhead.groovy")
ConfigObject config = new ConfigSlurper().parse(f.toURI().toURL())
log.info "Config: $config"
String appName = options.appName
String taName = config.taName

FusionClient fusionClient = new FusionClient(options)

def taCollection = fusionClient.getCollection(appName, taName)
if(taCollection){
    log.warn "Collection: $taName already exists, not recreating..."
} else {
    fusionClient.createCollection(taName)
}

config.blobs.each {String key, Object val ->
    String path = val.path
    String src = val.source
    Path blobFile = Paths.get(src)
    log.info "blob $key"
    String type = val.type
    def blobUpdate = fusionClient.blobUpload(appName, path, type, blobFile)
    log.info "result: $blobUpdate"
}


log.info "Done...?"
