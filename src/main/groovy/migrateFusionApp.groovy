import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.clients.FusionResponseWrapper
import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import com.lucidworks.ps.upval.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name);

OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)

log.info "start script ${this.class.name}..."

FusionClient fusionClient = new FusionClient(options)// todo -- revisit where/how to parse the source json (file, zip, or fusionSourceCluster...?), currently mixing approaches, need to clean up
if(options.test){
    log.info "Connection test mode (-t or --test arg given), skipping rest of processing/script..."
} else {
    File srcJson = fusionClient.objectsJsonFile

    Map parsedMap = ExtractFusionObjectsForIndexing.readObjectsJson(srcJson)
// process actual objects
    Map sourceFusionOjectsMap = parsedMap.objects
    log.info "\t\tObjects count: ${sourceFusionOjectsMap.size()} \n\t${sourceFusionOjectsMap.collect { "${it.key}(${it.value.size()})" }.join('\n\t')}"

    String group = fusionClient.objectsGroup ?: 'GeneralGroup'
//Map<String, String> appInfo = ExtractFusionObjectsForIndexing.getApplicationInfo(parsedMap, group)

    def keys = sourceFusionOjectsMap.keySet()
    List<Map<String, Object>> apps = sourceFusionOjectsMap['fusionApps']

// todo -- move this links stuff to the end, cleanup??
    List<Map<String, String>> oldLinks = sourceFusionOjectsMap['links']
    List<Map<String, String>> existingLinks = fusionClient.getLinks()
    Map<String, List> existingLinksMap = existingLinks.groupBy { Map m ->
        m.subject
    }

    if (apps.size() != 1) {
        log.warn "Returned apps object expected to be a single app, but it's size: ${apps.size()} -- apps obj: $apps"
    }

    apps.each { Map appMap ->
        String appName = appMap.name
        FusionResponseWrapper fusionResponseWrapper = fusionClient.addAppIfMissing(appMap)

        Map<String, List<Object>> connectorsStatus = fusionClient.addConnectorPluginsIfMissing(sourceFusionOjectsMap)
        log.debug "Connectors status: $connectorsStatus"

        List<FusionResponseWrapper> addCollResults = fusionClient.addCollectionsIfMissing(appName, sourceFusionOjectsMap)
        log.info "Add Collections Fusion Response wrappers (if any): $addCollResults"


        // --------------- Create DataSources ------------------
        List<FusionResponseWrapper> dsResponses = fusionClient.addDatasourcesIfMissing(appName, sourceFusionOjectsMap, oldLinks)
        log.info "Create datasources response wrappers (if any): $dsResponses"

        // --------------- Create Parsers ------------------
        def parsersToMigrate = sourceFusionOjectsMap['parsers']
        def parsersExisting = fusionClient.getParsers(appName)
        sourceFusionOjectsMap['parsers'].each { Map<String, Object> p ->
            String parserName = p.id
            def existingParser = parsersExisting.find { it.id == parserName }
            if (existingParser) {
                log.info "\tSkipping existing parser: $parserName"
            } else {
                def httpResponse = fusionClient.createParser(p, appName)
                log.info "Created parser: $parserName - response $httpResponse "
            }
        }


        // --------------- Create Index Pipelines ------------------
        def idxpExisting = fusionClient.getIndexPipelines('')
        sourceFusionOjectsMap['indexPipelines'].each { Map<String, Object> map ->
            String idxpName = map.id
            def existingIndexPipeline = idxpExisting.find { it.id == idxpName }
            if (existingIndexPipeline) {
                log.info "\tSkipping existing index pipeline $idxpName ... "
            } else {
                def httpResponse = fusionClient.createIndexPipeline(map, appName)
                log.info "Created index pipeline ($idxpName) "
            }
        }


        // --------------- Add missing Links (move down to end?) ------------------
        def dsLinksToCheck = oldLinks.findAll {
            it.subject.startsWith("datasource:") && it.object.startsWith("app:")
        }
        dsLinksToCheck.each { Map link ->
            def rc = fusionClient.createLink(link, appName)
            log.info "Create link result: $rc"
        }

    }
}

log.info "done...?"
