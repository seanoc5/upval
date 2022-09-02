package configs

import com.lucidworks.ps.clients.FusionClient

/**
 * Configuration exploriring how to list components and transformations for packaging
 * Aug 30
 */
// --------------------- variables defined below ----------------------

// --------------------- variables defined above ----------------------

// url for fusion to pull definitions or objects from:
source {
    // todo - look at alternative of github or zip Export in place of running fusion
    url = "${fusionUrl ?: 'http://foundry.lucidworksproserve.com:6764'}"
    user = "${userName ?: 'admin'}"
    password = "${password ?: 'password123'}"
    application = 'Components'
    gitRepo = 'https://raw.githubusercontent.com/seanoc5/upval/master/src/main/resources/typeahead'
}
destination {
    version = "${version ?: '5.5.1'}"
}
// below is valid and parsable, if needing spaces in key name
//destination.'help 3' = 'your help 3'

slurper = new groovy.json.JsonSlurper()
idxpUrl = "${source.gitRepo}/indexpipeline.main.v1.json".toURL()
idxpJson = idxpUrl.text
fusionClient = new FusionClient(source.url, source.user, source.password, source.application )

// --------------------- components/objects to grab, transform, and bundle ---------------------
objects {
    queryPipelines {
        // get 'main' user query pipeline from 'Components' app
        mainUserQueryPipeline = 'Components_TYPEAHEAD_DW_QPL_v4'
    }

    indexPipelines {
        fileUploadPipeline = 'Components_TYPEAHEAD_DW_IPL_v4'
        // testing getting json from url, slurping it, and including it in the config
//        testGithub = slurper.parse(idxpUrl)
    }

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

}

metadata {
    formatVersion = "1"
    exportedBy = "admin"
    exportedDate = "2021-07-09T22:28:12.910Z"
    fusionVersion = destination.version
    fusionGuid = "d62d4466-a46e-4948-97b4-58597712cc7e"
}

blobs {
    inclusionList {
        source = new File('/Users/sean/work/lucidworks/upval/src/main/resources/typeahead/Typeahead_inclusion_list.csv')

        unwantedTerms = new URL("${source.gitRepo}/excludeUnwantedTerms.js")
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


