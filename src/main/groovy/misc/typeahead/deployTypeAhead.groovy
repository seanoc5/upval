package misc.typeahead

import com.lucidworks.ps.clients.DeployArgParser
import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.clients.FusionResponseWrapper
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
final Logger log = Logger.getLogger(this.class.name)
log.info "Starting ${this.class.name} with args: ${args}..."


// ------------------ Configuration -------------------
OptionAccessor options = DeployArgParser.parse(this.class.name, args)
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
FusionClient fusionClient = new FusionClient(options)


// ------------------ Blobs -------------------
def existingBlobs
FusionResponseWrapper frw = fusionClient.getBlobs()
if (frw.wasSuccess()) {
    existingBlobs = frw.parsedList
} else {
    throw new IllegalArgumentException("Problem with call for blobs: $frw")
}
config.blobs.each { String key, Object val ->
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

// ------------------ Collections -------------------
def existingCollections = fusionClient.getCollections(appName)
if (existingCollections) {
    config.collections.each { String label, Map collectionConfig ->
        String destId = collectionConfig.id
        log.info "process Coll ID: $destId (label:$label) -> $collectionConfig"
        def taCollection = existingCollections.find { it.id == destId }
        if (taCollection) {
            log.warn "Collection: $taName already exists, not recreating : ${taCollection}"
        } else {
            log.info "Create new collection ($destId) in app: $appName -- solrParams: $collectionConfig"
            FusionResponseWrapper frwBCollection = fusionClient.createCollection(taName, collectionConfig, appName, false)
            log.info "FRW: $frwBCollection"
        }
    }
}

// ------------- DataSources --------------------
def existingDatasources = fusionClient.getDataSources(appName)
config.dataSources.each { String label, Map dsConfig ->
    String dsId = dsConfig.id
    log.info "Config Package Datasource ($label) with id: $dsId and keys: ${dsConfig.keySet()}"
    def destinationDS = existingDatasources.find {it.id == dsId }
    if (destinationDS) {
        log.info "Datasource ($label :: $dsId) already exists, skipping upload: $dsConfig"
    } else {
        log.info "Upload datasource: ($label :: $dsId)"
        FusionResponseWrapper frwDS = fusionClient.createDataSource(dsConfig, appName)
        log.info "\t\tDatasource ($dsId) response: $frwDS"
    }
}

log.info "Done...?"
