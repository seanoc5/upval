package misc

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import com.lucidworks.ps.upval.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/16/22, Monday
 * @description:
 */

import org.apache.log4j.Logger

final Logger log = Logger.getLogger(this.class.name);

OptionAccessor options = FusionClientArgParser.parse(this.class.name, args)

log.info "start script ${this.class.name}..."
FusionClient fusionClient = new FusionClient(options)// todo -- revisit where/how to parse the source json (file, zip, or fusionSourceCluster...?), currently mixing approaches, need to clean up

File srcJson = fusionClient.objectsJsonFile
Map parsedMap = ExtractFusionObjectsForIndexing.readObjectsJson(srcJson)
def dataSources = parsedMap.objects.dataSources
StringBuilder stringBuilder = new StringBuilder()
def pwdConnectors = dataSources.each {Map m ->
    m.properties.each { String key, def val ->
        String s = val.toString()
        if (s.containsIgnoreCase('${')) {
            log.info "Variable string: $s"
            stringBuilder.append(s + "\n")
        }
    }
}

File outFile = new File(fusionClient.exportDirectory, 'passwords.groovy')
outFile.text = stringBuilder.toString()
log.info "Done...?"
