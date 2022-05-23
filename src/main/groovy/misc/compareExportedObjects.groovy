package misc

import com.lucidworks.ps.compare.DataSources
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
File srcF4ObjectsJson = new File('/home/sean/work/lucidworks/Intel/objects.json')
Map f4Objects = jsonSlurper.parse(srcF4ObjectsJson)
log.info "Original Json:\n\t\t$f4Objects"

File srcF5ObjectsJson = new File('/home/sean/work/lucidworks/Intel/F5/objects.json')
Map f5Objects = jsonSlurper.parse(srcF5ObjectsJson)
log.info "Altered Json:\n\t\t$f5Objects)"

/*
DiffNode root = ObjectDifferBuilder.buildDefault().compare(f4Objects, f5Objects)
log.info "Diff node: $root"
root.visit(new DiffNode.Visitor() {
    public void node(DiffNode node, Visit visit) {
        final Object baseValue = node.canonicalGet(origMap);
        final Object workingValue = node.canonicalGet(alteredMap);
        log.info "Node path: ${node.getPath()}  state: ${node.getState()}: ORIGINAL value: $baseValue -- ALTERED value: $workingValue"
    }
})
*/

DataSources


log.info "Done...?"
