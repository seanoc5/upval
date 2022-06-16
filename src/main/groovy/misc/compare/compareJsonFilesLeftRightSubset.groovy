package misc.compare

import com.lucidworks.ps.compare.CompareObjectResults
import com.lucidworks.ps.upval.BaseComparatorArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonSlurper
import org.apache.log4j.Logger
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/23/22, Monday
 * @description:
 */
final Logger log = Logger.getLogger(this.class.name);

log.info "Starting ${this.class.name}..."

OptionAccessor options = BaseComparatorArgParser.parse(this.class.name, args)

JsonSlurper jsonSlurper = new JsonSlurper()
File leftObjectsJson = new File(options.left)
Map leftObjects = jsonSlurper.parse(leftObjectsJson)
log.info "LEFT Json:\n\t\t${leftObjects.keySet()}"

File rightObjectsJson = new File(options.right)
Map rightObjects = jsonSlurper.parse(rightObjectsJson)
log.info "RIGHT  Json:\n\t\t${rightObjects.keySet()})"

CompareObjectResults objectsResults = new CompareObjectResults("ldap-acls", leftObjects, rightObjects)
log.info "Results: ${objectsResults.toString()}"


log.info "Done...?"
