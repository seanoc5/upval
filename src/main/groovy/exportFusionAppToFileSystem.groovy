import com.lucidworks.ps.Helper
import com.lucidworks.ps.clients.ExportedAppArgParser
import com.lucidworks.ps.model.BaseObject
import com.lucidworks.ps.model.fusion.Application
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonGenerator
import org.apache.log4j.Logger

/**
 * test script to proof-out the process of exporting "all the things" from an fusion app (assuming app export zip as source)
 */
Logger log = Logger.getLogger(this.class.name)

log.info "start script ${this.class.name}..."

OptionAccessor options = ExportedAppArgParser.parse(this.class.name, args)
File appZipFile = new File(options.source)
File exportFolder = options.exportDir ? new File(options.exportDir) : null
boolean groupedExport = true
if(options.flat){
    groupedExport = false
    log.info "Option 'flat' was given, overriding default grouped output, no grouping in object type folders based on the 'flat' option given..."
}

// http://man.hubwiz.com/docset/Groovy.docset/Contents/Resources/Documents/groovy/json/JsonGenerator.Options.html
def jsonCustomOutput = new JsonGenerator.Options()
        .dateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .build()


if (!appZipFile?.canRead()) {
    throw new IllegalArgumentException("Could not find valid source file: ${options.source} (should be an app export zip file)")
} else {
    List<String> thingsToExport = Application.DEFAULT_APP_OBJECTS
    Application app = new Application(appZipFile)
    thingsToExport.each { String typeName ->
        def exportable = app.getThings(typeName)
        if(exportable) {
            log.info "---------------- Export Things of type: $typeName (classname: ${exportable.getClass().simpleName}) ----------------"
            File outFolder = exportFolder
            if (exportable instanceof BaseObject) {
                if (groupedExport) {
                    outFolder = Helper.getOrMakeDirectory(exportFolder, typeName)
                }
                ((BaseObject) exportable).export(outFolder)
                log.info "exported BaseObject: $exportable"
            } else if (exportable instanceof Collection) {
                Collection exp = exportable
                log.warn "Exportable (${exp.size()}) is of type: (${exp.getClass().simpleName}) "
                exportable.each {

                }
            } else if (exportable instanceof Map) {
                log.info "\t\t$typeName) Map to export: ${exportable.keySet().size()}"
                exportable.each { String label, def collection ->
                    String s = jsonCustomOutput.toJson(collection)
                    String outName = "${typeName}.${label}.json"
                    File outfile = new File(exportFolder, outName)
                    outfile.text = s
                    log.info "Wrote file (${outfile.absolutePath}) with Map (${collection.getClass().simpleName}) size: ${collection.size()}"
                }

            } else {
                log.warn "$typeName) UNKNOWN type: $typeName: $exportable"
            }
        } else {
            log.info "Tried getting things of type: $typeName, but found nothing... skipping..."
        }
    }
    log.debug "Done iterating types: $thingsToExport"
}

log.info "done...?"
