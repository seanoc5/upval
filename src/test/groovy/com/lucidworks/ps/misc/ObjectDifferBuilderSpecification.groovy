package com.lucidworks.ps.misc

import de.danielbechler.diff.ObjectDifferBuilder
import de.danielbechler.diff.node.DiffNode
import de.danielbechler.diff.node.Visit
import de.danielbechler.diff.selector.ElementSelector
import spock.lang.Specification

/**
 * Exploring ObjectDifferBuilder approach
 * @deprecated
 * @see com.lucidworks.ps.mapping.ObjectTransformerJayway
 */
class ObjectDifferBuilderSpecification extends Specification {


    def "compareTestJsonStrings"() {
        given:
        Map origMap = [
                topleaf  : 'one',
                topParent: [secondLeaf: 'two-one']
        ]
        Map alteredMap = [
                topleaf  : 'one',
                topParent: [secondLeaf: 'two-one altered'],
                newLeaf:'not in origMap'
        ]

        when:
        DiffNode masterDiffNode = ObjectDifferBuilder.buildDefault().compare(alteredMap, origMap)
        Map<ElementSelector, DiffNode> changes = masterDiffNode.children
        Set<ElementSelector> changeKeys = changes.keySet()
        ElementSelector newLeafKey = changeKeys[0]
        def newLeafDiffNode = changes.get(newLeafKey)
        ElementSelector topParentKey = changeKeys[1]
        def topParentDiffNode = changes.get(topParentKey)
        StringBuilder stringBuilder = new StringBuilder()
        masterDiffNode.visit(new DiffNode.Visitor() {
            public void node(DiffNode node, Visit visit) {
                final Object baseValue = node.canonicalGet(origMap);
                final Object workingValue = node.canonicalGet(alteredMap);
                final String message = "${node.getPath()} changed from ${baseValue} to ${workingValue}"
                System.out.println(message);
                stringBuilder.append(message + '\n')
            }
        });

        then:
        masterDiffNode.state == DiffNode.State.CHANGED
        masterDiffNode.childCount() == 2
        newLeafKey.toString() == '{newLeaf}'
        topParentKey.toString() == '{topParent}'
        stringBuilder.toString() == '''/ changed from [topleaf:one, topParent:[secondLeaf:two-one]] to [topleaf:one, topParent:[secondLeaf:two-one altered], newLeaf:not in origMap]
/{newLeaf} changed from null to not in origMap
/{topParent} changed from [secondLeaf:two-one] to [secondLeaf:two-one altered]
/{topParent}{secondLeaf} changed from two-one to two-one altered
'''
    }

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

//    JsonSlurper jsonSlurper = new JsonSlurper()
//        Map origMap = jsonSlurper.parseText(origJson)
//        Map alteredMap = jsonSlurper.parseText(alteredJson)
//        root.visit(new DiffNode.Visitor() {
//            public void node(DiffNode node, Visit visit) {
//                final Object baseValue = node.canonicalGet(origMap);
//                final Object workingValue = node.canonicalGet(templateJson);
//                log.info "Node path: ${node.getPath()}  state: ${node.getState()}: base(old/src) value: $baseValue -- working value (template): $workingValue"
//            }
//        })

}
