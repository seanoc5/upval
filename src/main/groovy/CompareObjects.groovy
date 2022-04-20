package misc

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

String origJson = '''
{
    'topLeaf':'one',
    'topParent': { 
        'secondLeaf':'two-one'
    }
}    
'''

String alteredJson = '''
{
    "topLeaf":"one",
    "topParent": { 
        "firstLeaf":"two-one-new",
        "addedLeaf":"found in orginal alteredJson source"        
    }
}    
'''

JsonSlurper jsonSlurper = new JsonSlurper()
Map origMap = jsonSlurper.parseText(origJson)
Map alteredMap = jsonSlurper.parseText(alteredJson)


DiffNode root = ObjectDifferBuilder.buildDefault().compare(origMap, templateJson)
log.info "Diff node: $root"
root.visit(new DiffNode.Visitor() {
    public void node(DiffNode node, Visit visit) {
        final Object baseValue = node.canonicalGet(origMap);
        final Object workingValue = node.canonicalGet(templateJson);
        log.info "Node path: ${node.getPath()}  state: ${node.getState()}: base(old/src) value: $baseValue -- working value (template): $workingValue"
    }
})
log.info "test: " + templateJson.properties.crawlDBType

log.info "Done...?"




// --------------------------- Snippets ----------------------
