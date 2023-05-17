import com.lucidworks.ps.Helper
import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.config.FusionClientArgParser
import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonBuilder
import groovy.transform.Field
import org.apache.log4j.Logger

import java.text.SimpleDateFormat

//import com.lucidworks.ps.upval.Fusion4ObjectTransformer

@Field
Logger log = Logger.getLogger(this.class.name)

log.info "start script ${this.class.name}..."
OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)
log.info "start script ${this.class.name}..."
FusionClient fusionClient = new FusionClient(options)

File srcJson = fusionClient.objectsJsonFile
Map parsedMap = ExtractFusionObjectsForIndexing.readObjectsJson(srcJson)
Map sourceFusionOjectsMap = parsedMap.objects
String appName = sourceFusionOjectsMap.fusionApps[0].id
Date now = new Date()
String versionFolderName = Helper.getVersionName(now)
String fooFolderName = Helper.getVersionName(now, new SimpleDateFormat('yyyy-MM-dd/hh-mm'))     // todo -- pick one or the other format for export folder naming...

def jobsWithTriggers = sourceFusionOjectsMap.jobs.findAll { it.triggers?.size() }
File exportDir = fusionClient.exportDirectory
File commands = new File(exportDir, 'curl-commands.sh')
commands.text = "export fuser='${fusionClient.user}'\nexport fpass='${fusionClient.password}'\nexport furl='${fusionClient.fusionBase}'\n\n"          // clear any previous content

jobsWithTriggers.each { Map<String, Object> job ->
    String resource = job.resource
    log.info "Processing job resource: $resource..."

    log.debug "Job): $job"
    if (job.triggers.size() > 1) {
        log.info "${resource} Multipe job triggerss: ${job.triggers}"
    }
    String json = null
    if (exportDir) {
        def parts = resource.split(':')
        String type = parts[0]
        String name = parts[1]

        // todo -- do we want to split date and time? e.g. /20220616/10-2/
        File jobFolder = Helper.getOrMakeDirectory("${exportDir}/${versionFolderName}/jobs/$type/$name")
        File fooFolder = Helper.getOrMakeDirectory("${exportDir}/${fooFolderName}/jobs/$type/$name")

        File f = new File(jobFolder, "${name}.json")
        Map triggers = [triggers: job.triggers]
        JsonBuilder builder = new JsonBuilder(triggers)
        json = builder.toPrettyString()
        log.info "\t\tWriting trigger json file: ${f.absolutePath}"
        log.debug "\t\ttrigger json: ${json}"
        f.text = json

        String furl = fusionClient.fusionBase
        String cmd = "curl -u \$fuser:\$fpass -X PUT -H \"Content-Type: application/json\" \$furl/api/jobs/$type:$name/schedule -d '${builder.toString()}'\n\n"
        commands << cmd
    }

}
log.info "Wrote commands file: ${commands.absolutePath}"

log.info "Done...?"



