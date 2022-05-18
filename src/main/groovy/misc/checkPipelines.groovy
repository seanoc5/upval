package misc

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import com.lucidworks.ps.upval.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name);

OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)

log.info "start script ${this.class.name}..."
FusionClient fusionClient = new FusionClient(options)

File srcJson = fusionClient.objectsJsonFile
Map parsedMap = ExtractFusionObjectsForIndexing.readObjectsJson(srcJson)
Map sourceFusionOjectsMap = parsedMap.objects

List<Map<String,Object>> indexPipelines = sourceFusionOjectsMap.indexPipelines
def stages = indexPipelines.collect {it.stages}.flatten()
def skipped = stages.findAll{it.skip}

def indexStageTypes = stages.groupBy{it.type}
log.info "Index pipeline stage types:\n" + indexStageTypes.collect {String key, def val -> "${val.size()} :: $key"}.join('\n')
File indexOutFile = new File("index.javascript.js")
indexOutFile.text = ''      // clear contents (if any)
def jsIndexTypes = ['javascript-index', 'managed-js-index']
jsIndexTypes.each {String type ->
    List<Map<String, Object>> checkStages = indexStageTypes[type]
    checkStages.each {
        String label = it.label ?: 'unknown label'
        queryOutFile << "// ---------------- $label --------------------\n"
        String script = it.script ?: 'missing script??'
        queryOutFile << script
        queryOutFile << '\n'
    }
}


def querystages = sourceFusionOjectsMap.queryPipelines.collect {it.stages}.flatten()
def queryStageTypes = querystages.groupBy{it.type}
log.info "Query pipeline stage types:\n" + queryStageTypes.collect {String key, def val -> "${val.size()} :: $key"}.join('\n')

File queryOutFile = new File("query.javascript.js")
queryOutFile.text = ''      // clear contents (if any)
def jsTypes = ['javascript-query', 'managed-js-query']
jsTypes.each {String type ->
    List<Map<String, Object>> checkStages = queryStageTypes[type]
    checkStages.each {
        String label = it.label ?: 'unknown label'
        queryOutFile << "// ---------------- $label --------------------\n"
        String script = it.script ?: 'missing script??'
        queryOutFile << script
        queryOutFile << '\n'
    }
}
log.info " Wrote file: ${queryOutFile.absolutePath}"
log.info "done...?"
