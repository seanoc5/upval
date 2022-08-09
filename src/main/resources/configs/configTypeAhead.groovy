//package configs

// name of side-car collection, and blob folder, part of other element names
taName = "${taName ?: 'typeahead'}"
appName = "$appName"
baseId = "${appName}_$taName"


blobs {
    serviceLib {
        type = 'file'
        path = 'lib/index/FusionServiceLib.js'
        source = 'src/main/resources/typeahead/FusionServiceLib.js'
    }
    badWords {
        type = 'file'
        path = "${ta_name}/full-list-of-bad-words_csv-file_2018_07_30.csv"
        source = 'src/main/resources/typeahead/full-list-of-bad-words_csv-file_2018_07_30.csv'
    }
    inclusionList {
        type = 'file'
        path = "${ta_name}/Typeahead_inclusion_list.csv"
        source = 'src/main/resources/typeahead/Typeahead_inclusion_list.csv'
    }
    //{{furl}}/api/blobs/lib/index/FusionServiceLib.js
}

collections {
    typeahead {
        id = "${baseId}"
        searchClusterId = "default"
        commitWithin = 10000
        solrParams {
            name = "${baseId}"
            numShards = 1
            replicationFactor = 2
            maxShardsPerNode = 2
        }
        type = "DATA"
        metadata = {}
    }
}


// testing groovy config rather than json... incomplete
indexPipelines {
    main {
        id = "${baseId}_IPL"
        stages = [
                [
                        id                 : "b3736ea6-c8a8-4180-950a-1c5f47a80f49",
                        ref                : "lib/index/FusionServiceLib.js",
                        type               : "managed-js-index",
                        skip               : false,
                        label              : "MJS: load FusionServices.js",
                        secretSourceStageId: "b3736ea6-c8a8-4180-950a-1c5f47a80f49"
                ],
                [
                        id                          : "f6724c11-2e15-4184-ae70-b762ab87ef85",
                        translationMappings         : [
                                [
                                        source   : "Suggestion",
                                        target   : "value_s",
                                        operation: "move",
                                ], [
                                        source   : "Weight",
                                        target   : "signal_count",
                                        operation: "move",
                                ]
                        ],
                        retentionMappings           : [],
                        updateMappings              : [],
                        unmappedRule                : [
                                keep                      : true,
                                delete                    : false,
                                fieldToMoveValuesTo       : "",
                                fieldToCopyValuesTo       : "",
                                valueToAddToUnmappedFields: "",
                                valueToSetOnUnmappedFields: "",
                        ],
                        reservedFieldsMappingAllowed: false,
                        type                        : "field-mapping",
                        skip                        : false,
                        label                       : "Field Mapping for Inclusion List Suggestions",
                        secretSourceStageId         : "f6724c11-2e15-4184-ae70-b762ab87ef85",
                ]
        ]
    }

}


//dataSources {
//    {
//        "id" : "${foundry.destination.APP}_${foundry.FEATURE_NAME}_inclusion_list"
//        "created" : "2021-04-26T23:25:09.167Z"
//        "modified" : "2021-04-26T23:25:09.167Z"
//        "connector" : "lucid.fileupload"
//        "type" : "fileupload"
//        "pipeline" : "${foundry.destination.APP}_${foundry.FEATURE_NAME}_IPL"
//        "parserId" : "_system"
//        "properties" : {
//        "collection" : "${foundry.destination.APP}_${foundry.FEATURE_NAME}"
//        "fileId" : "${foundry.FEATURE_NAME}/Typeahead_inclusion_list.csv"
//        "mediaType" : "text/csv"
//    }
//    }
