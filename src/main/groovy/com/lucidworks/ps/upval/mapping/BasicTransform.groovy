package com.lucidworks.ps.upval.mapping

import javax.json.Json;
import javax.json.stream.JsonParser;

/**
 * Basic class for working through dynamic function invokation
 * adding more logic and work flow
 */
class BasicTransform {

    static String convertFoo(foo){
        return "In foo($foo)"
    }

    static def convertJsonPointer(String srcPath, String destPath, def srcJson, def destJson){

    }
}
