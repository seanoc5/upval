package com.lucidworks.ps.upval.mapping


/**
 * test class for working through dynamic function invocation
 * very simple test
 */
class SimpleTransform extends ObjectTransformer{

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

    SimpleTransform(Map srcMap, Map destMap, Map transConfig, String separator) {
        super(srcMap, destMap, transConfig, separator)
    }

    static String foo(bar){
        return "In foo($bar)"

    }

}
