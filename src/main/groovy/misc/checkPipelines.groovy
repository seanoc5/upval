package misc

import com.lucidworks.ps.Helper
import com.lucidworks.ps.clients.ExportedAppArgParser
import com.lucidworks.ps.model.fusion.Application
import com.lucidworks.ps.model.fusion.Javascript
import groovy.cli.picocli.OptionAccessor
import groovy.io.FileType
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.common.SolrInputDocument

//import com.lucidworks.ps.fusion.Application

Logger log = Logger.getLogger(this.class.name)

OptionAccessor options = ExportedAppArgParser.parse(this.class.name, args)

log.info "start script ${this.class.name}..."

//FusionClient fusionClient = new FusionClient(options)


String host = 'newmac'
int port = 8983
String collection = 'lucy'
String baseUrl = "http://$host:$port/solr/$collection"
//SolrClient solrClient = new HttpSolrClient(baseUrl)
SolrClient solrClient = new HttpSolrClient.Builder(baseUrl).build();
log.info "Created solr client (to index directly to solr with url: '$baseUrl') -- solrClient: $solrClient"
SolrQuery sq = new SolrQuery('*:*')
def respTest = solrClient.query(sq)

File srcFolder = new File(options.source)
if (srcFolder && srcFolder.canRead() && srcFolder.isDirectory()) {
    log.info "Source Folder: ${srcFolder.absoluteFile}"
} else {
    throw new IllegalArgumentException("Source option (${options.source}) not a folder, this script expects to check a folder full of fusion app exports")
}

File exportDir = Helper.getOrMakeDirectory(options.exportDir)

srcFolder.eachFileMatch(FileType.FILES, ~/.*\.zip/) { File appFile ->
    String fileBaseName = FilenameUtils.getBaseName(appFile.toString())
    Application application = new Application(appFile)

    log.info "App File (${fileBaseName}): $appFile"
    String appLabel = "${fileBaseName}.${application.appName}"
    if (application && application.indexPipelines) {
        Map<String, List<Javascript>> idxJsStages = application.indexPipelines.javascriptStages
        Map<String, List<Javascript>> qryJsStages = application.queryPipelines.javascriptStages

        def allJSStages = idxJsStages + qryJsStages

        File appExportDir = new File(exportDir, fileBaseName)
        Helper.getOrMakeDirectory(appExportDir)

        List<SolrInputDocument> sidList = []
        allJSStages.each { String key, List<Javascript> jsList ->
            jsList.each { Javascript jsStage ->
                String label = jsStage.label
                File f = new File(exportDir, label + ".js")
                f.text = jsStage.script
                log.info "$key) (${jsStage.lines.size()}) lines -> ${f.absolutePath}"
                SolrInputDocument sid = jsStage.toSolrInputDocument()
                sid.addField('_lw_data_source_s', this.class.name)
                sid.addField('appName_txt', appLabel)
                sidList << sid
            }
        }
        if (sidList) {
            UpdateResponse resp = solrClient.add(sidList, 1000)
            log.info "$appFile) Sent (${sidList.size()}) solr docs (JS STages) to solr, response: $resp"
        } else {
            log.warn "---------- NO JS Stages! ${appFile.absolutePath}"
        }
    } else {
        log.warn "Not a valid Fusion app!!? ${appFile.absolutePath}"
    }

}
