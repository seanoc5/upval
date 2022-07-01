package misc


import com.lucidworks.ps.model.Application
import com.lucidworks.ps.upval.ExportedAppArgParser
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

/**
 * Testing script to get a datasource collection, and explode (future "export" functionality) onto the filesystem
 */
Logger log = Logger.getLogger(this.class.name)
OptionAccessor options = ExportedAppArgParser.parse(this.class.name, args)
log.info "start script ${this.class.name}..."

//FusionClient fusionClient = new FusionClient(options)
File srcFile = new File(options.source)
if(srcFile && srcFile.canRead()){
    log.info "Source file: ${srcFile.absoluteFile}"
}

Application application = new Application(srcFile)
Map fusionParsedMap = application.parsedMap

Map<String, Object> objects = fusionParsedMap.objects
def dataSources = objects.dataSources
def brokenDataSources = dataSources.findAll {it.created.contains('EDT')}
brokenDataSources.each {
    String name = it.id
//    String
    log.info "Datasource: $it"
}
