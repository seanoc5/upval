package com.lucidworks.ps.upval.mapping

import javax.json.Json;
import javax.json.stream.JsonParser;

/**
 * test class for working through dynamic function invokation
 */
class BasicTransform {

    static String convertFoo(foo){
        return "In foo($foo)"
    }

    static def convertJsonPointer(String srcPath, String destPath, def srcJson, def destJson){

    }
}
