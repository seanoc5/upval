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
}


// ------------------- helper utils for config reading/processing -------------------
slurper = new groovy.json.JsonSlurper()
// exampLe of config-is-code: create a working helper class to get configs directly from a running fusion (i.e. foundry)
fusionClient = new FusionClient(source.url, source.user, source.password, source.application)


// --------------------- components/objects to grab, transform, and bundle ---------------------
objects {
    // switch from 'typical' config syntax like "objects {..}" to setting
    // a variable/leaf-node with standard groovy assignment (necessary to set a list rather than map)
    queryPipelines = [
            fusionClient.getQueryPipeline('Components_TYPEAHEAD_DW_QPL_v4', source.application),
    ]

    indexPipelines = [
            fusionClient.getIndexPipeline('Components_TYPEAHEAD_DW_IPL_v4', source.application),
    ]

    /*
    collections {

    }

    dataSources {

    }

    features {

    }

    queryProfiles {

    }

    tasks {

    }

    blobs {

    }

    sparkJobs {

    }
*/
}

metadata {
    formatVersion = "1"
    exportedBy = "admin"
    exportedDate = "2021-07-09T22:28:12.910Z"
    fusionVersion = destination.version
    fusionGuid = "d62d4466-a46e-4948-97b4-58597712cc7e"
}

blobs {
    inclusionCsv {
//        source = new File('/Users/sean/work/lucidworks/upval/src/main/resources/typeahead/Typeahead_inclusion_list.csv')
    }
    unwantedTerms {
//        source = new URL("${source.gitRepo}/excludeUnwantedTerms.js")
    }
}


/*
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
        [
                "id"    : "foundry.FEATURE_NAME",
                "schema": [
                        "type"       : "string",
                        "title"      : "FEATURE_NAME",
                        "description": "FEATURE_NAME",
                        "hints"      : []
                ]
        ],
        [
                "id"    : "foundry.typeahead.TYPE_FIELD_1",
                "schema": [
                        "type"       : "string",
                        "title"      : "TYPE_FIELD_1",
                        "description": "TYPE_FIELD_1",
                        "hints"      : []
                ]
        ],
        [
                "id"    : "foundry.typeahead.TYPE_FIELD_2",
                "schema": [
                        "type"       : "string",
                        "title"      : "TYPE_FIELD_2",
                        "description": "TYPE_FIELD_2",
                        "hints"      : []
                ]
        ],
        [
                "id"    : "foundry.typeahead.TYPE_FIELD_3",
                "schema": [
                        "type"       : "string",
                        "title"      : "TYPE_FIELD_3",
                        "description": "TYPE_FIELD_3",
                        "hints"      : []
                ]
        ],
        [
                "id"    : "foundry.typeahead.TYPE_FIELD_4",
                "schema": [
                        "type"       : "string",
                        "title"      : "TYPE_FIELD_4",
                        "description": "TYPE_FIELD_4",
                        "hints"      : []
                ]
        ],
        [
                "id"    : "foundry.typeahead.TYPE_FIELD_5",
                "schema": [
                        "type"       : "string",
                        "title"      : "TYPE_FIELD_5",
                        "description": "TYPE_FIELD_5",
                        "hints"      : []
                ]
        ],
        [
                "id"    : "foundry.destination.APP",
                "schema": [
                        "type"       : "string",
                        "title"      : "APP",
                        "description": "APP",
                        "hints"      : []
                ]
        ],
        [
                "id"    : "foundry.destination.COLLECTION",
                "schema": [
                        "type"       : "string",
                        "title"      : "COLLECTION",
                        "description": "COLLECTION",
                        "hints"      : []
                ]
        ]
]
*/


// NOTE: below syntax is valid and parsable, if needing spaces in key name
//destination.'help 3' = 'your help 3'
