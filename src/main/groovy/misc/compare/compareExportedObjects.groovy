package misc.compare

import com.lucidworks.ps.compare.FusionObjectComparator
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


JsonSlurper jsonSlurper = new JsonSlurper()
File srcF4ObjectsJson = new File('/home/sean/work/lucidworks/Intel/exports/objects.F4.json')
Map f4Objects = jsonSlurper.parse(srcF4ObjectsJson)
log.info "Original Json:\n\t\t${f4Objects.keySet()}"

File srcF5ObjectsJson = new File('/home/sean/work/lucidworks/Intel/exports/objects.F5.json')
Map f5Objects = jsonSlurper.parse(srcF5ObjectsJson)
log.info "Altered Json:\n\t\t${f5Objects.keySet()})"


FusionObjectComparator comparator = new FusionObjectComparator('dataSources', f4Objects.objects.dataSources, f5Objects.objects.dataSources)
log.info "Results: ${comparator.toString()}"


log.info "Done...?"
