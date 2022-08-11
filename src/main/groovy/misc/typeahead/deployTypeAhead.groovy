package misc.typeahead

import com.lucidworks.ps.clients.FusionClient
import groovy.json.JsonSlurper
import groovy.xml.XmlParser
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

/*OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)
URL configUrl = getClass().getResource(options.config)

String appName = options.appName
String taName = options.taName
ConfigSlurper configSlurper = new ConfigSlurper()
Map bindingMap=[appName:appName]
if(taName){
    bindingMap['taName'] = taName
    log.info "Found typeahead name from CLI/options: $taName -- set binding to override config file...."
}
configSlurper.setBinding(bindingMap)
ConfigObject config = configSlurper.parse(configUrl)
log.info "Config: $config"*/
File pkgSource = new File(getClass().getResource('/typeahead/').toURI())

log.info "File f: ${pkgSource.absolutePath}"

File xmlComponentSrc = new File(pkgSource,'Features_TYPEAHEAD_DW_F5.4_v4_component-def_TYPEAHEAD_DW_F5.4_v4.xml')
File jsonVariablesSrc = new File(pkgSource,'Features_TYPEAHEAD_DW_F5.4_v4_variables.json')
File jsonObjectsSrc = new File(pkgSource,'Features_TYPEAHEAD_DW_F5.4_v4_Feature_TYPEAHEAD_DW_F5.4_v4_objects.json')
//Map taCollectionDef = config.collections.typeahead

JsonSlurper jsonSlurper = new JsonSlurper()
Map jsonVariables = jsonSlurper.parse(jsonVariablesSrc)
Map jsonObjects = jsonSlurper.parse(jsonObjectsSrc)

XmlParser xmlParser = new XmlParser()
def xmlComponents = xmlParser.parse(xmlComponentSrc)

log.info "What do we have?"



FusionClient fusionClient = new FusionClient(options)
def taCollection = fusionClient.getCollection(appName, taName)
if(taCollection){
    log.warn "Collection: $taName already exists, not recreating..."
} else {
    fusionClient.createCollection(taName, taCollectionDef, appName, false)
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