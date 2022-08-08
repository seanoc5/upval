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

log.info "Starting ${this.class.name}..."

OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)
Path cfgUrl = Paths.get(options.config)
File f = new File("/home/sean/work/lucidworks/upval/src/main/resources/configs/configTypeAhead.groovy")
ConfigObject config = new ConfigSlurper().parse(f.toURI().toURL())
log.info "Config: $config"

FusionClient fusionClient = new FusionClient(options)

config.blobs.each {String key, Object val ->
    String path = val.path
    String src = val.source
    File blobFile = new File(src)
    log.info "blob $key"
    def blobUpdate = fusionClient.blobUpload(path, blobFile)
}


log.info "Done...?"
