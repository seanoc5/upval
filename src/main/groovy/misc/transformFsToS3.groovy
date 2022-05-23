package misc

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import com.lucidworks.ps.upval.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name);

OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)

log.info "start script ${this.class.name}..."
FusionClient fusionClient = new FusionClient(options)// todo -- revisit where/how to parse the source json (file, zip, or fusionSourceCluster...?), currently mixing approaches, need to clean up

File srcJson = fusionClient.objectsJsonFile
Map parsedMap = ExtractFusionObjectsForIndexing.readObjectsJson(srcJson)
Map sourceFusionOjectsMap = parsedMap.objects
log.info "\t\tSource Fusion Objects count: ${sourceFusionOjectsMap.size()} \n\t${sourceFusionOjectsMap.collect { "${it.key}(${it.value.size()})" }.join('\n\t')}"

String appName = options.appName

List<Map<String,Object>> sourceJobs = sourceFusionOjectsMap.jobs
def srcJobNames = sourceJobs.collect {it.resource}

def targetJobs = fusionClient.getJobs(appName)
//def targetJobName = targetJobs.collect {it.resource}
def targetJobSchedules = fusionClient.getJobSchedules(targetJobs)

def diff = srcJobNames - targetJobName
log.info "Compare oldjobs (${sourceJobs.size()}) to existing jobs: ${sourceJobs}"

log.info "done...?"
