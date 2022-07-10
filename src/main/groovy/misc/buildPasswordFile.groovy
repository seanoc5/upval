package misc

import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import com.lucidworks.ps.clients.FusionClientArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonBuilder

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
Map passwordEntries = [:]
parsedMap.properties.findAll{
    String id = it.id
    if(id.startsWith('secret')){
        passwordEntries[id] = ''
    }
}

File outFile = new File(fusionClient.exportDirectory, 'passwords.json')
outFile.text = ''           // clear previous content



//def dataSources = parsedMap.objects.dataSources

/*
def pwdConnectors = dataSources.each { Map m ->
    m.properties.each { String key, def val ->
        String s = val.toString()
        if (s.containsIgnoreCase('${')) {
            log.info "Variable string: $s"
            def matches = (s =~ /\$.([^\}]+)}/).findAll()
            matches.each {
                String matchedKey = it[1]
                passwordEntries["$matchedKey"] = ""
//                passwordEntries << map
//                stringBuilder.append(it[1] + " = \n")
            }
        }
    }
}
*/
JsonBuilder jsonBuilder = new JsonBuilder(passwordEntries)
outFile.text = jsonBuilder.toPrettyString()
//outFile.text = stringBuildertoString()

log.info "Done...?"
