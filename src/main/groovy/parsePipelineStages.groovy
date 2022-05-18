import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import com.lucidworks.ps.upval.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.transform.Field
import org.apache.log4j.Logger

//import com.lucidworks.ps.upval.Fusion4ObjectTransformer

@Field
Logger log = Logger.getLogger(this.class.name);

log.info "start script ${this.class.name}..."
OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)
log.info "start script ${this.class.name}..."
FusionClient fusionClient = new FusionClient(options)

File srcJson = fusionClient.objectsJsonFile
Map parsedMap = ExtractFusionObjectsForIndexing.readObjectsJson(srcJson)
Map sourceFusionOjectsMap = parsedMap.objects
String appName = sourceFusionOjectsMap.fusionApps[0].id

def indexPipelines = sourceFusionOjectsMap.indexPipelines
def idxpTypes = ['field-mapping':'easy']
def stages = []
indexPipelines.each {Map idxp ->
    idxp.stages.each {
        stages << it
    }
}
def groupedStages = stages.groupBy {it.type}
idxpTypes.collect {it.type}

log.info "Done...?"



// --------------------- Functions -------------------------------
public File getOrMakeDirectory(String dirPath) {
    File folder = new File(dirPath)
    if (folder.exists()) {
        if (folder.isDirectory()) {
            log.debug "Folder (${folder.absolutePath} exists, which is good"
        } else {
            log.warn "Folder (${folder.absolutePath}) exists, but is not a folder, which is bad"
            throw new IllegalAccessException("Job Folder (${folder.absolutePath}) exists, but is not a folder, which is bad, aborting")
        }
    } else {
        def success = folder.mkdirs()
        if (success) {
            log.info "\t\tCreated folder: ${folder.absolutePath}"
        } else {
            log.warn "Folder (${folder.absolutePath}) could not be created, which is bad"
            throw new IllegalAccessException("Folder (${folder.absolutePath}) exists, could not be created which is bad, aborting")
        }
    }
    folder
}
