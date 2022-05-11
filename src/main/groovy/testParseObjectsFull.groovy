import com.lucidworks.ps.clients.FusionClient
//import com.lucidworks.ps.upval.Fusion4ObjectTransformer
import groovy.json.JsonSlurper
import org.apache.http.client.config.RequestConfig
import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name);

log.info "start script ${this.class.name}..."
// ------------ info for saving extracted information back to a fusion store for further analysis -----------
URL baseUrl = new URL('http://newmac:8764')
String user = 'sean'                            // fusion username for saving content back to fusi
String pass = 'pass1234'
String destApp = 'lucy'
String destColl = 'upval'
String destProfile = 'upval-json'


// todo replace hardcoded files with cli args
Map sourceFilesMap = [
'todo--replace me'
]

int timeout = 5
RequestConfig requestConfig = RequestConfig.custom()
  .setConnectTimeout(timeout * 1000)
  .setConnectionRequestTimeout(timeout * 1000)
  .setSocketTimeout(timeout * 1000).build();


boolean deletePreviousAppDocs = true

sourceFilesMap.each { sourceOwner, path ->
//File objectsJsonFile = new File("C:/work/Lucidworks/CDW/active_products/objects.json")
    File objectsJsonFile = new File(path)
    log.info "parsing file: ${objectsJsonFile.absolutePath}"
    if (objectsJsonFile.exists()) {
        String jsonString = Fusion4ObjectTransformer.readObjectsJson(objectsJsonFile)
        JsonSlurper slurper = new JsonSlurper()
        Map parsedMap = slurper.parseText(jsonString)
        // process actual objects
        Map objects = parsedMap.objects
        log.info "\t\tObjects count: ${objects.size()} \n\t${objects.collect { "${it.key}(${it.value.size()})" }.join('\n\t')}"

        Map<String, String> appInfo = Fusion4ObjectTransformer.getApplicationInfo(parsedMap, objectsJsonFile, sourceOwner)


        FusionClient fusionClient = new FusionClient(baseUrl, user, pass)
        log.info "Fusion client setup: $fusionClient"
        String q = "appGuid_s:\"${appInfo.appGuid}\""
        if (deletePreviousAppDocs) {
            log.warn "Deleting old/previous application docs in fusion/solr, delete query: $q"
            def delRsp = fusionClient.deleteByQuery(destColl, q, true)
            log.info "\t\tDelete response: $delRsp"
        }

        objects.each { String key, def val ->
            try {
                // groovy dynamic method cal -- https://docs.groovy-lang.org/latest/html/documentation/core-metaprogramming.html#_dynamic_method_names
                log.info "DYNAMIC FUNCTION '$key', val size:${val.size()}  in object: Fusion4ObjectTransformer..."
                Collection things = Fusion4ObjectTransformer."${key}"(val, appInfo)
                log.debug "\t\tresulting collection size: ${things.size()}"

                def rsp = fusionClient.indexContentByProfile(things, destApp, destProfile, true)
                String body = rsp.body()
                def bodyMap = slurper.parseText(body)
                def docs = bodyMap.docs
                log.info "\t\tCheck index response: ${rsp.statusCode()} -- indexed doc count: ${docs.size()}"
                log.debug "\t\tresults docs: $docs"

            } catch (Exception e) {
                log.error "Error: $e"
            }
        }

        // show metadata
        Map metadata = parsedMap.metadata
        log.info "metadata count: ${metadata.size()} -- ${metadata}"

    } else {
        log.warn "no objects.json file exists at: ${objectsJsonFile.absolutePath}"
    }
}

log.info "done...?"
