package misc


import com.lucidworks.ps.upval.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.log4j.Logger
/**
 * Testing script to get a datasource collection, and explode (future "export" functionality) onto the filesystem
 */
Logger log = Logger.getLogger(this.class.name)
log.info "start script ${this.class.name}..."

OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)

//FusionClient fusionClient = new FusionClient(options)
File srcFile = new File('/home/sean/Downloads/Untitled(1).js')

//FusionClient fusionClient = new FusionClient(options)
File exportFolder = new File('/home/sean/work/lucidworks/upval/out')
exportFolder.mkdirs()

// http://man.hubwiz.com/docset/Groovy.docset/Contents/Resources/Documents/groovy/json/JsonGenerator.Options.html
def jsonDefaultOutput = new JsonGenerator.Options()
.dateFormat('YYYY-MM-DD\'T\'HH:mm:ss.sssZ')
        .build()

JsonSlurper slurper = new JsonSlurper()
def dataSources = slurper.parse(srcFile)
def brokenDataSources = dataSources.findAll {it.created.contains(' EDT ')}
brokenDataSources.each {
    String name = it.id
    String type = it.type
    String filename = "${type}.${name}.json"
    log.info "Datasource: $it"
    String json = JsonOutput.toJson(it)
    File outFile = new File(exportFolder, filename)
    outFile.text = json

    String json2 = jsonDefaultOutput.toJson(it)
    outFile = new File(exportFolder, 'iso-' + filename)
    outFile.text = json2

    log.info "wrote file: ${outFile}"
}
