import com.lucidworks.ps.Helper
import com.lucidworks.ps.model.BaseObject
import com.lucidworks.ps.model.fusion.Application
import com.lucidworks.ps.upval.ExportedAppArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import org.apache.log4j.Logger
/**
 * test script to proof-out the process of exporting "all the things" from an fusion app (assuming app export zip as source)
 */
Logger log = Logger.getLogger(this.class.name)

log.info "start script ${this.class.name}..."

OptionAccessor options = ExportedAppArgParser.parse(this.class.name, args)
File appZipFile = new File(options.source)
File exportFolder = options.exportDir ? new File(options.exportDir) : null

// http://man.hubwiz.com/docset/Groovy.docset/Contents/Resources/Documents/groovy/json/JsonGenerator.Options.html
def jsonCustomOutput = new JsonGenerator.Options()
        .dateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .build()


if (!appZipFile?.canRead()) {
    throw new IllegalArgumentException("Could not find valid source file: ${options.source} (should be an app export zip file)")
} else {
    List<String> thingsToExport = Application.DEFAULT_APP_OBJECTS
//    thingsToExport = "collections indexPipelines queryPipelines parsers".split(' ')
//    List<String> thingsToExport = FusionApplicationComparator.DEFAULTTHINGSTOCOMPARE
    Application app = new Application(appZipFile)
    thingsToExport.each { String typeName ->
        def exportable = app.getThings(typeName)
        log.debug "Export Things of type: $typeName (classname: ${exportable.getClass().simpleName})"

        if (exportable instanceof Collection) {
            log.info "$typeName) Collection to export: ${exportable.size()}"
            int i = 0
            exportable.each {   //String label, Collection collection ->
                i++
                String label
                if(it instanceof Map){
                    if(it.id) {
                        label = it.id
                    } else if(it.resource){         // handle JOBs here
                        label = "${it.resource}"
                        log.debug "$typeName) Handling label special case: $label"
                    } else if(it.linkType){
                        label = "${it.linkType}-${it.subject}-${it.object}"
                        log.debug "$typeName) Handling label special case: $label"
                    } else {
                        log.warn "$typeName) unknown export label, going with counter of objects (probably bad for diff'ing)"
                        label = "$i"
                    }

                } else {
                    label = "${i}"
                    log.warn "$typeName) export with item counter in name rather than item id; can't find id attrib in object: ${it.getClass().simpleName}"
                }
                String jsonString = jsonCustomOutput.toJson(it)
                String prettyJson = JsonOutput.prettyPrint(jsonString)
                String outName = Helper.sanitizeFilename("${typeName}.${label}.json")
                // todo -- get better naming for list items...
                File outfile = new File(exportFolder, outName)
                outfile.text = prettyJson
                log.debug "Wrote file (${outfile.absolutePath}) with collection (${it.getClass().simpleName}) size: ${it.size()}"
            }
            log.debug "finished exporting collection type: $typeName."
        } else if (exportable instanceof Map) {
            log.info "\t\t$typeName) Map to export: ${exportable.keySet().size()}"
            exportable.each { String label, Collection collection ->
                String s = jsonCustomOutput.toJson(collection)
                String outName = "${typeName}.${label}.json"
                File outfile = new File(exportFolder, outName)
                outfile.text = s
                log.info "Wrote file (${outfile.absolutePath}) with Map (${collection.getClass().simpleName}) size: ${collection.size()}"
            }
        } else if (exportable instanceof BaseObject) {
            ((BaseObject) exportable).export(exportFolder)
            log.info "exported BaseObject: $exportable"
        } else {
            log.warn "$typeName) UNKNOWN type: $typeName: $exportable"
        }
    }

}

log.info "done...?"
