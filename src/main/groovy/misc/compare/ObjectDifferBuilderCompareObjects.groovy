package misc.compare

import com.lucidworks.ps.upval.mapping.SimpleTransform
import de.danielbechler.diff.ObjectDifferBuilder
import de.danielbechler.diff.node.DiffNode
import de.danielbechler.diff.node.Visit
import groovy.json.JsonSlurper
import org.apache.log4j.Logger
/**
 * Script to compare two (json origin) objects
 */

Logger log = Logger.getLogger(this.class.name);

log.info "Starting script ${this.class.name}..."


JsonSlurper jsonSlurper = new JsonSlurper()
Map origMap = jsonSlurper.parseText(SimpleTransform.origJson)
log.info "Original Json:\n\t\t$origMap"

Map alteredMap = jsonSlurper.parseText(SimpleTransform.alteredJson)
log.info "Altered Json:\n\t\t$alteredMap"

DiffNode root = ObjectDifferBuilder.buildDefault().compare(origMap, alteredMap)
log.info "Diff node: $root"
root.visit(new DiffNode.Visitor() {
    public void node(DiffNode node, Visit visit) {
        final Object baseValue = node.canonicalGet(origMap);
        final Object workingValue = node.canonicalGet(alteredMap);
        log.info "Node path: ${node.getPath()}  state: ${node.getState()}: ORIGINAL value: $baseValue -- ALTERED value: $workingValue"
    }
})

log.info "Done...?"




// --------------------------- Snippets ----------------------
