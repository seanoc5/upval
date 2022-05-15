package misc
/**
 * @author : sean
 * @mailto : seanoc5@gmail.com
 * @created : 5/8/22, Sunday
 * */


import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.upval.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

final Logger log = Logger.getLogger(this.class.name)

log.info "Starting ${this.class.name}..."

OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)
FusionClient fusionClient = new FusionClient(options)

def apps = fusionClient.getApplications()
def myApp = fusionClient.getApplication('test')
String sampleJobResource = 'datasource:campus_db'
Map<String, Object> rsp = fusionClient.getJob(sampleJobResource)
int responseCounter = 0
log.info "Client request/responses..."
fusionClient.responses.each {
    responseCounter++
    log.info "\t\t$responseCounter) ${it.toString()}"
}

log.info "Done...?"
