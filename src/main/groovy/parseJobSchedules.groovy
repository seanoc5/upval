import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import com.lucidworks.ps.upval.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonBuilder
import groovy.transform.Field

//import com.lucidworks.ps.upval.Fusion4ObjectTransformer

import org.apache.log4j.Logger

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

def jobsWithTriggers = sourceFusionOjectsMap.jobs.findAll { it.triggers?.size() }
File exportDir = fusionClient.exportDirectory
File commands = new File(exportDir, 'curl-commands.sh')
commands.text = "export fuser='${fusionClient.user}'\nexport fpass='${fusionClient.password}'\nexport furl='${fusionClient.fusionBase}'\n\n"          // clear any previous content

jobsWithTriggers.each { Map<String, Object> job ->
    String resource = job.resource
    log.info "Processing job resource: $resource..."

    log.debug "Job): $job"
    if (job.triggers.size() > 1) {
        log.info "${resource} Multipe jobs: ${job.triggers}"
    }
    String json = null
    if (exportDir) {
        def parts = resource.split(':')
        String type = parts[0]
        String name = parts[1]

        File jobFolder = getOrMakeDirectory("${exportDir}/jobs/$type/$name")

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