import com.lucidworks.ps.model.fusion.Application
import com.lucidworks.ps.model.BaseObject
import com.lucidworks.ps.upval.ExportedAppArgParser
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

/**
 * test script to proof-out the process of exporting "all the things" from an fusion app (assuming app export zip as source)
 */
Logger log = Logger.getLogger(this.class.name)

log.info "start script ${this.class.name}..."

OptionAccessor options = ExportedAppArgParser.parse(this.class.name, args)
File appZipFile = new File(options.source)
File exportFolder = options.exportDir ? new File(options.exportDir) : null
if(exportFolder){
    if(exportFolder.exists()){
        if(exportFolder.isDirectory()){
            log.info "Using export folder: $exportFolder"
        } else {
            throw new IllegalArgumentException("Export destinationmust be a folder (not a file/symlink?): ${exportFolder.absolutePath} ")
        }
    } else {
        log.info "export folder (${exportFolder.absolutePath}) does not exist, make it now..."
        boolean created = exportFolder.mkdirs()
        if(!created){
            throw new IllegalArgumentException("Cannot create destination folder: ${exportFolder.absolutePath} ")
        }
    }
} else {
    log.info "No export folder given..."
}

if (!appZipFile?.canRead()) {
    throw new IllegalArgumentException("Could not find valid source file: ${options.source} (should be an app export zip file)")
} else {
    List<String> thingsToExport = "configsets collections dataSources indexPipelines queryPipelines parsers".split(' ')
//    List<String> thingsToExport = FusionApplicationComparator.DEFAULTTHINGSTOCOMPARE
    Application app = new Application(appZipFile)
    thingsToExport.each { String typeName ->
        log.info "Export Things of type: $typeName"
        def exportable = app.getThings(typeName)
        if(exportable instanceof Collection) {
            log.info "\t\t$typeName) Collection to export: ${exportable.size()}"
        } else if (exportable instanceof Map){
            log.info "\t\t$typeName) Map to export: ${exportable.keySet().size()}"
        } else if (exportable instanceof BaseObject){
            ((BaseObject)exportable).export()
            log.debug "exported object: $exportable"
        } else {
            log.warn "$typeName) UNKNOWN type: $typeName: $exportable"
        }
    }

}

log.info "done...?"
