package configs

import com.lucidworks.ps.clients.FusionClient

/**
 * Configuration exploring how to list components and transformations for packaging
 *
 */

// -------------------  params to configure source (mainly fusion-j) for fusion to pull definitions or objects from: -------------------
source {
    url = "${fusionUrl ?: 'http://foundry.lucidworksproserve.com:6764'}"
    user = "${userName ?: 'admin'}"
    password = "${password ?: 'password123'}"
    application = "${appName ?: 'Components'}"
}


// ------------------- destination-specific settings -------------------
destination {
    version = "${version ?: '5.5.1'}"
    outputZipPath = "${exportDir: '.'}"            // todo -- decide on string or actual file...
}

// testing/experimenting: can we make this replace (or build) the 'properties' section in the objects (objects.json) below...?
propVariables {
    ZKHOST {
        path = '$..sparkJobs.readObtions[@key="zkHost"]'
        title = 'ZK Connect String'
        description = 'ZK Connect String for clicks load spark job (and any solr connectors)'
    }
    SIGNALS_AGGR_COLL {
        path = '$..sparkJobs.readObtions[@key="collection"].value'
        description = 'ZK Connect String for clicks load spark job (and any solr connectors)'

    }
}



// ------------------- helper utils for config reading/processing -------------------
slurper = new groovy.json.JsonSlurper()
// exampLe of config-is-code: create a working helper class to get configs directly from a running fusion (i.e. foundry)
fusionClient = new FusionClient(source.url, source.user, source.password, source.application)


// --------------------- components/objects to grab, transform, and bundle: typically objects.json, configsets, and blobs  ---------------------
objectsJson {
    // awkward naming/structure: file is called `objects.json` and has 3 top-level trees called: objects, blobs, configsets
    // NB: objectsJson.objects.blobs is the definition of blob objects, the "actual" blobs are "files" under: objectsJson.blobs
    objects {
        // switch from 'typical' config syntax like "objects {..}" to setting
        // a variable/leaf-node with standard groovy assignment (necessary to set a list rather than map)
        queryPipelines = [
                fusionClient.getQueryPipeline('Components_TYPEAHEAD_DW_QPL_v4', source.application),
        ]

        indexPipelines = [
                fusionClient.getIndexPipelines(source.application, 'Components_TYPEAHEAD_DW_IPL_v4'),
        ]


        collections = [
            fusionClient.getCollectionDefinition(source.application, 'Components_TYPEAHEAD_DW_v4'),
        ]

        dataSources = [
//        fusionClient.getDataSource('Components_TYPEAHEAD_DW_IPL_v4', source.application),
        ]

        features {

        }

        queryProfiles {

        }

        tasks {

        }

        blobs = [
                fusionClient.getBlobDefinitions(source.application, 'file',),
/*
            // hard coded version for reference
            [
                    "id"          : "${foundry.FEATURE_NAME}/Typeahead_inclusion_list.csv",
                    "path"        : "/${foundry.FEATURE_NAME}/Typeahead_inclusion_list.csv",
                    "dir"         : "/${foundry.FEATURE_NAME}",
                    "filename"    : "Typeahead_inclusion_list.csv",
                    "contentType" : "text/csv",
                    "size"        : 110,
                    "modifiedTime": "2021-06-07T23:43:24.148Z",
                    "version"     : 1701953566565990400,
                    "md5"         : "49e87771204fca511c26852fb229b6e5",
                    "metadata"    : ["resourceType": "file"]
            ],
            [
                    "id"          : "${foundry.FEATURE_NAME}/full-list-of-bad-words_csv-file_2018_07_30.csv",
                    "path"        : "/${foundry.FEATURE_NAME}/full-list-of-bad-words_csv-file_2018_07_30.csv",
                    "dir"         : "/${foundry.FEATURE_NAME}",
                    "filename"    : "full-list-of-bad-words_csv-file_2018_07_30.csv",
                    "contentType" : "text/csv",
                    "size"        : 26846,
                    "modifiedTime": "2021-06-07T23:43:24.437Z",
                    "version"     : 1701953566867980288,
                    "md5"         : "58592b144f5584625942a1f617d2761f",
                    "metadata"    : ["resourceType": "file"]
            ],
            [
                    "id"          : "lib/index/FusionServiceLib.js",
                    "path"        : "/lib/index/FusionServiceLib.js",
                    "dir"         : "/lib/index",
                    "filename"    : "FusionServiceLib.js",
                    "contentType" : "text/javascript",
                    "size"        : 9866,
                    "modifiedTime": "2021-06-11T17:58:12.025Z",
                    "version"     : 1702294236196503552,
                    "md5"         : "231d5da713875ea1b94c88638810a974",
                    "metadata"    : ["resourceType": "file:js-index"]
            ]

 */
        ]


        sparkJobs {

        }


    }


    metadata {
        formatVersion = "1"
        exportedBy = "admin"
        exportedDate = "2021-07-09T22:28:12.910Z"
        fusionVersion = destination.version
        fusionGuid = "d62d4466-a46e-4948-97b4-58597712cc7e"
    }

    properties = [
            [
                    "id"    : "foundry.typeahead.ZKHOST",
                    "schema": [
                            "type"       : "string",
                            "title"      : "ZKHOST",
                            "description": "ZKHOST",
                            "hints"      : []
                    ]
            ],
            [
                    "id"    : "foundry.destination.SIGNALS_AGGR_COLL",
                    "schema": [
                            "type"       : "string",
                            "title"      : "SIGNALS_AGGR_COLL",
                            "description": "SIGNALS_AGGR_COLL",
                            "hints"      : []
                    ]
            ],
    ]
}
// will be compiled into zipfile at 'top' level, next to objects.json and configsets folder
blobs {
    inclusionCsv {
//        source = new File('/Users/sean/work/lucidworks/upval/src/main/resources/typeahead/Typeahead_inclusion_list.csv')
    }
    unwantedTerms {
//        source = new URL("${source.gitRepo}/excludeUnwantedTerms.js")
    }
}

configsets {

}

