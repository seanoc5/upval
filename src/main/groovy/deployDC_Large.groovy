import com.lucidworks.ps.Helper
import com.lucidworks.ps.clients.ExportedAppArgParser
import com.lucidworks.ps.model.BaseObject
import com.lucidworks.ps.model.fusion.Application
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonOutput
import groovy.transform.Field
import org.apache.log4j.Logger
/**
 * exploratory script, pulling together several Fusion-j calls to get an overview of a Fusion install
 */
@Field
Logger log = Logger.getLogger(this.class.name);
log.info "start Fusion app assessment (for complexity) script: ${this.class.name}..."

OptionAccessor options = ExportedAppArgParser.parse(this.class.name, args)
File src = new File(options.source)
File exportDir = null
if(options.exportDir){
    exportDir = Helper.getOrMakeDirectory( new File(options.exportDir))
} else {
    log.info "No export dir given, not saving assessment..."
}
Application app = new Application(src)


    def things = app.getThings(type)
    if(things) {
        log.info "\t\t ---------------------- Process type: $type --------------------------"
        if(things instanceof BaseObject){
            BaseObject fusionObject = (BaseObject)things
            def assessment = fusionObject.assessComplexity()
            fullAssessment[type] = assessment
        } else {
            log.info "Things of type($type) are not extended from BaseObject, so we need to assess 'manually'..."
            if(things instanceof Collection){
                log.warn "\t\tTODO:: Assess ($type) collection: ${things.size()}"
            } else if(things instanceof Map){
                log.warn "\t\tTODO:: Assess ($type) MAP, with keys: ${things.keySet()}"
            } else {
                log.warn "\t\tTODO:: Assess ($type) something UNKNOWN?? -- ${things}"
            }
        }
    } else {
        log.warn "No thing: $type, which is fine unless you were expecting something back...?"
    }
}

int sumComplexity = 0
int sumItems = 0
int sumTypes = 0
fullAssessment.each { String thing, Map map ->
    log.info "Assessment of $thing with (${map.size}) items, and rough guess complexity: ${map.complexity}"
    sumComplexity+= map.complexity
    sumTypes++
    sumItems+= map.size
    map.items?.each {
        log.debug "\t\tassesment item: $it"
    }
}
if(exportDir){
    File outfile = new File(exportDir, "assessment.json")
    String json = JsonOutput.toJson(fullAssessment)
    outfile.text = JsonOutput.prettyPrint(json)
    log.info "Wrote assessment to file: ${outfile.absoluteFile}"
}

log.info "Sum complexity: $sumComplexity with "

log.info "done...?"


// ---------------------- stuff ------------------------

Map assessCollections(List<Map> collections) {
    Set<String> clusterIds = []

    Map<String, Object> sizeItem = [:]
    int sizeComplexity = collections.size() / 5

    Map collectionsAssessment = [type: 'Colllections', items:[]]
    collectionsAssessment.collectionsSize = [message: "Size of collections (${collections.size()})", complexity: sizeComplexity]
    sizeItem.complexity = sizeComplexity
    collectionsAssessment.collectionSize = sizeItem
    int totalComplexity = 0

    collections.each { Map collMap ->
        int complexity = 0
        String clusterId = collMap.searchClusterId
        clusterIds << clusterId
        Map solrParams = collMap.solrParams
        String name = solrParams.name
        Map collComplexity = [name: name]
        if (name.containsIgnoreCase('alias')) {
            complexity++
            Map item = [message: "${name}) Alias in name, added complexity: 1", complexity: 1]
            collComplexity.alias = item
        } else {
            log.debug "No alias, ignore..."
//            collComplexity.alias = [message: "Not alias, no added compexity", complexity: 0]
        }

        log.debug "\t\tProcessing collection: ${name.padRight(30)}  --  clusterid ($clusterId)"
        int commitWithin = collMap.commitWithin
        if (commitWithin < 1000) {
            Map item = [:]
            if (commitWithin < 500) {
                item.complexity = 5
                complexity += 5
                item.message = "Commitwithin < 500, could mean complexity(?)"
            } else {
                item.complexity = 1
                complexity++
                item.message = "Commitwithin < 1000, could mean complexity(?)"
            }
            collComplexity.commitWith = item
        } else {
            collComplexity.commitWith = [message: "Commitwith ($commitWithin) normal, no added compexity", complexity: 0]
        }

        // todo parse createdAt, check for age/outdated

        int numShards = solrParams?.numShards ?: 0
        if (numShards > 3) {
            int i = numShards / 3
            complexity += i
            collComplexity.numShards = [message: "NumShards ($numShards) added complexity: $i", complexity: i]
        } else {
            collComplexity.numShards = [message: "NumShards ($numShards) normal, no added compexity", complexity: 0]
        }

        int replicationFactor = solrParams?.replicationFactor ?: 0
        if (replicationFactor > 3) {
            int i = replicationFactor / 3
            complexity += i
            collComplexity.replicationFactor = [message: "Replication factor ($replicationFactor) added complexity: $i", complexity: i]
        } else {
            collComplexity.replicationFactor = [message: "Replication factor ($replicationFactor) normal, no added compexity", complexity: 0]
        }

        String type = collMap.type
        if (type.equalsIgnoreCase('data')) {
            complexity++
            collComplexity.type = [message: "$name) type ${type} (data?) adding minor complexity of 1", complexity: 1]
        } else {
            collComplexity.type = [message: "Type ($type) normal, no added compexity", complexity: 0]
        }

        collComplexity.complexity = complexity
        log.info "\t\tcollection: ${name.padLeft(30)}  :: complexity (${collComplexity.complexity}) ::: $collComplexity"

        collectionsAssessment.items << collComplexity
        totalComplexity += complexity
    }

    collectionsAssessment.complexity = totalComplexity
    log.info "combinedAssessment: ${collectionsAssessment}"
    collectionsAssessment.items.each {Map m ->
        log.info "Collection: ${m.name}: ${m.complexity}"
    }

    return collectionsAssessment
}
