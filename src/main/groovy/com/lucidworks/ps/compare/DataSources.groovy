package com.lucidworks.ps.compare

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/23/22, Monday
 * @description: helper class to compare two different Lists of datasources
 * we use the terminology source and destination losely, but the aim is to help understand the difference reporting
 */

class DataSources {
    List<Map> source = []
    List<Map> destination = []
    def differences = [
            countDiffs:0,
            idDiffs:[sourceOnly:[], destinationOnly:[]],
            typeDiffs:[]

    ]

    DataSources(List<Map<String, Object>> src, List<Map<String, Object>> dest) {
        source = src
        destination = dest
    }

    def compare(){
        differences.countDiffs = source.size() - destination.size()

        def srcIds = source.collect{it.id}
        def destIds = destination.collect{it.id}
        def srcOnlyIds = srcIds - destIds
        def destOnlyIds = destIds - srcIds

    }
}
