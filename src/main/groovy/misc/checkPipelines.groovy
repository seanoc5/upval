package misc

import com.lucidworks.ps.Helper
import com.lucidworks.ps.clients.ExportedAppArgParser
import com.lucidworks.ps.model.fusion.Application
import com.lucidworks.ps.model.fusion.Javascript
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.common.SolrInputDocument

//import com.lucidworks.ps.fusion.Application

Logger log = Logger.getLogger(this.class.name)

OptionAccessor options = ExportedAppArgParser.parse(this.class.name, args)

log.info "start script ${this.class.name}..."

//FusionClient fusionClient = new FusionClient(options)

File srcFile = new File(options.source)
if(srcFile && srcFile.canRead()){
    log.info "Source file: ${srcFile.absoluteFile}"
}

Application application = new Application(srcFile)
Map fusionParsedMap = application.exportedObjectsSourceMap

//Map<String, Map<String, Object>> idxp = application.indexPipelines.pipelines.collectEntries {k, v -> v.each {it. = 'index'; return [k, v] }
//Map<String, Map<String, Object>> idxStages = application.indexPipelines.pipelineStagesMap

Map<String, List<Javascript>> idxJsStages = application.indexPipelines.javascriptStages
//Map<String, Map<String, Object>> qryStages = application.queryPipelines.pipelineStagesMap
Map<String, List<Javascript>> qryJsStages = application.queryPipelines.javascriptStages
//def qstgTypeGrouped = qryp.values()*.stages*.type.flatten().groupBy {it}

File exportDir = Helper.getOrMakeDirectory(options.exportDir)
def allJSStages = idxJsStages + qryJsStages

String host = 'newmac'
int port = 8983
String collection = 'lucy'
String baseUrl = "http://$host:$port/solr/$collection"
//SolrClient solrClient = new HttpSolrClient(baseUrl)
SolrClient solrClient = new HttpSolrClient.Builder(baseUrl).build();

List< SolrInputDocument> sidList = []
allJSStages.each { String key, List<Javascript> jsList ->
    jsList.each { Javascript jsStage ->
        String label = jsStage.label
        File f = new File(exportDir, label + ".js")
        f.text = jsStage.script
        log.info "$key) (${jsStage.lines.size()}) lines -> ${f.absolutePath}"
        SolrInputDocument sid = jsStage.toSolrInputDocument()
        sidList << sid
    }
}

UpdateResponse resp = solrClient.add(sidList, 1000)
log.info "Sent (${sidList.size()}) solr docs (JS STages) to solr, response: $resp"


//log.info "Index pipeline stage types:\n" + indexStageTypes.collect { String key, def val -> "${val.size()} :: $key" }.join('\n')
//File indexOutFile = new File("index.javascript.js")
//indexOutFile.text = ''      // clear contents (if any)
//def jsIndexTypes = ['javascript-index', 'managed-js-index']
//
//jsIndexTypes.each { String stageType ->
//    List<Map<String, Object>> checkStages = indexStageTypes[stageType]
//}
