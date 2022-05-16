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

def stageTypes = stages.groupBy{it.type}

log.info "done...?"
