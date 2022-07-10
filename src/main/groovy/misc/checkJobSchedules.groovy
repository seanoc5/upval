package misc

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import com.lucidworks.ps.clients.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name);

OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)
log.info "start script ${this.class.name}..."
FusionClient fusionClient = new FusionClient(options)

File srcJson = fusionClient.objectsJsonFile
Map parsedMap = ExtractFusionObjectsForIndexing.readObjectsJson(srcJson)
Map sourceFusionOjectsMap = parsedMap.objects

String appName = options.appName

List<Map<String,Object>> sourceJobs = sourceFusionOjectsMap.jobs
def srcJobSchedules = sourceJobs.findAll {it.triggers}

def foo = fusionClient.addJobSchedulesIfMissing(appName, srcJobSchedules, true)

def destJobs = fusionClient.getJobs(appName)
def destSchedules = fusionClient.getJobSchedules(destJobs)
def responses = fusionClient.getResponses()
responses.each {
    log.debug "Save Response? ${it.response}"
}
log.info "done...?"


/*
String json = '''
{
  "resource" : "datasource:intel-com_web",
  "enabled" : true,
  "triggers" : [ {
    "type" : "cron",
    "enabled" : true,
    "expression" : "0 30 12 ? * MON-FRI",
    "type" : "cron"
  } ],
  "default" : false
}
'''
JsonSlurper slurper = new JsonSlurper()
Map<String, Object> jsonMap = slurper.parseText(json)


def newScheduleFRW = fusionClient.createJobSchedule(jsonMap, appName)
log.info "Fusion response: $newScheduleFRW"
*/
