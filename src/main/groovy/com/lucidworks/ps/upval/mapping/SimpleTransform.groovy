package com.lucidworks.ps.upval.mapping


/**
 * test class for working through dynamic function invocation
 * very simple test
 */
class SimpleTransform extends ObjectTransformer{

    SimpleTransform(Map srcMap, Map destMap, Map transConfig, String separator) {
        super(srcMap, destMap, transConfig, separator)
    }

    static String foo(bar){
        return "In foo($bar)"

    }

}
