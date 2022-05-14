import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import com.lucidworks.ps.upval.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor

//import com.lucidworks.ps.upval.Fusion4ObjectTransformer

import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name);

log.info "start script ${this.class.name}..."
OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)
log.info "start script ${this.class.name}..."
FusionClient fusionClient = new FusionClient(options)

File srcJson = fusionClient.objectsJsonFile
Map parsedMap = ExtractFusionObjectsForIndexing.readObjectsJson(srcJson)
Map sourceFusionOjectsMap = parsedMap.objects
String appName = sourceFusionOjectsMap.fusionApps[0].id

def jobsWithTriggers = sourceFusionOjectsMap.jobs.findAll {it.triggers?.size()}
jobsWithTriggers.each {Map<String, Object> job ->
    log.debug "Job): $job"
    if(job.triggers.size() > 1){
        log.info "Multipe jobs: ${job.triggers}"
    }
}

log.info "done...?"
