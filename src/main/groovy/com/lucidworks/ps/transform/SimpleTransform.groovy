package com.lucidworks.ps.transform


/**
 * test class for working through dynamic function invocation
 * very simple test
 * @deprecated this started with the idea of custom modifications in a class, but perhaps this is unnessary? pass enclosures instead?...
 */
class SimpleTransform extends ObjectTransformerJayway {

    public static final String origJson = '''
{
    "topLeaf":"one",
    "topParent": { 
        "firstLeaf":"two-one"
    }
}    
'''

    public static final String alteredJson = '''
{
    "topLeaf":"one",
    "topParent": { 
        "firstLeaf":"two-one-new",
        "addedLeaf":"found in orginal alteredJson source"        
    }
}    
'''

    SimpleTransform(Map srcMap, Map destMap, Map rules) {
        super(srcMap, destMap, rules)
    }

    static String testDescription(bar) {
        return "Creating a test description for thing: ($bar)"
    }

}
