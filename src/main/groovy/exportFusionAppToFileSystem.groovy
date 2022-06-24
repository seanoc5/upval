import com.lucidworks.ps.fusion.Application
import com.lucidworks.ps.upval.ExportedAppArgParser
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name)

log.info "start script ${this.class.name}..."

OptionAccessor options = ExportedAppArgParser.parse(this.class.name, args)
File appZipFile = new File(options.source)
if (!appZipFile?.canRead()) {
    throw new IllegalArgumentException("Could not find valid source file: ${options.source} (should be an app export zip file)")
}
Application app = new Application(appZipFile)


log.info "done...?"
