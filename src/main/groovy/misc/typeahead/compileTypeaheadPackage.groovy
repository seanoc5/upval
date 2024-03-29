package misc.typeahead

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.config.DeployArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonOutput
import groovy.transform.Field
import org.apache.log4j.Logger

import java.nio.file.Path
import java.nio.file.Paths
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   8/4/22, Thursday
 * @description: read the typeahead config file, perform string template build to get 'customized' objects.json
 *
 */
@Field
final Logger log = Logger.getLogger(this.class.name)
log.info "Starting ${this.class.name} with args: ${args.findAll {!it.startsWith('-p') }}..."


// ------------------ Configuration -------------------
OptionAccessor options = DeployArgParser.parse(this.class.name, args)
//File cfgFile = new File(options.config)       // clibuilder casts arg to file, no need to do so again here...
File cfgFile = options.config
if(cfgFile.exists()){
    log.info "Reading config file: ${cfgFile.absolutePath}"
} else {
    String msg = "Can't find config file: ${cfgFile.absolutePath}!! throwing error..."
    log.warn msg
    throw new IllegalArgumentException(msg)
}

String appName = options.appName
String taName = options.taName
ConfigSlurper configSlurper = new ConfigSlurper()
Map bindingMap = [appName: appName]
if (taName) {
    bindingMap['taName'] = taName
    log.info "Found typeahead name from CLI/options: $taName -- set binding to override config file...."
}
configSlurper.setBinding(bindingMap)
URL configUrl = cfgFile.toURI().toURL()
log.info "ConfigSlurper using: $configUrl"
ConfigObject config = configSlurper.parse(configUrl)
log.debug "ConfigObject: $config"
Map objects = config.objects
def indexPipelines = objects.indexPipelines

// ------------------ Fusion Client -------------------
FusionClient fusionClient = new FusionClient(options)


// ------------------ Blobs -------------------
deployBlobs(fusionClient, config, appName)

String outPath = options.exportDir
if(outPath) {
    log.info "Using exportDir: $outPath"
} else {
    log.warn "Could not find exportDir in options, defaulting to './'  "
    outPath = './'
}
File outDir = com.lucidworks.ps.Helper.getOrMakeDirectory(outPath)
File outFile = new File(outDir, "TA-${config.APP}.objects.json")
String json = JsonOutput.toJson(config.objects)
outFile.text = JsonOutput.prettyPrint(json)
log.info "Wrote objects.json to: ${outFile.absolutePath}"

log.info "Done...?"



// -------------------- Functions ---------------------
public void deployBlobs(FusionClient fusionClient, ConfigObject config, String appName) {
    def existingBlobs = fusionClient.getBlobDefinitions()
//    if (frw.wasSuccess()) {
//        existingBlobs = frw.parsedList
//    } else {
//        throw new IllegalArgumentException("Problem with call for blobs: $frw")
//    }
    config.objects.getBlobDefinitions.each { String key, Object val ->
        def destinationBlob = existingBlobs.find { it.path == val.path }
        if (destinationBlob) {
            log.info "Destination blob already exists, not re-uploading: key: $key --blob:$val"
        } else {
            String path = val.path
            String src = val.source
            Path blobFile = Paths.get(getClass().getResource(src).toURI())
            log.info "blob $key -- path: ${blobFile.toString()} -> abs path: ${blobFile.toAbsolutePath()}"
            String type = val.type
            def blobUpdate = fusionClient.blobUpload(appName, path, type, blobFile)
            log.info "result: $blobUpdate"
        }
    }
}
