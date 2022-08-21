package configs

import groovy.json.JsonSlurper

variables {
    FEATURE_NAME = "MyTypeAhead"
    APP = "MyApp"
    COLLECTION = "MyAppColl"
    ZKHOST = "myzk-0.myzk-headless=2181,myzk-1.myzk-headless=2181,myzk-2.myzk-headless=2181"
    SIGNALS_AGGR_COLL = "SIGNALS_AGGR_COLLECTION"
    TYPE_FIELD_1 = "TYPE_FIELD_1"
    TYPE_FIELD_2 = "TYPE_FIELD_2"

    baseId = "${APP}_${FEATURE_NAME}"
}

objects {
    queryPipelines {
        // load an external file (most common approach...?
        signalsHistory = new JsonSlurper().parse(new File('./src/test/resources/components/typeahead/querypipeline.main.v1.json'))
    }

    indexPipelines {
        // load an external file (most common approach...?
        main = new JsonSlurper().parse(new File('./src/test/resources/components/typeahead/indexpipeline.main.v1.json'))
    }

    collections {
        // define the object here, an alternative to loading an 'external' file...
        sidecar {
            id = "${variables.baseId}"
            searchClusterId = 'default'
            commitWithin = 10000
            solrParams {
                name = "${variables.baseId}"
                numShards = numShards
                replicationFactor = replicationFactor
                maxShardsPerNode = maxShardsPerNode
            }
            type = 'DATA'
            metadata = []
        }
    }

    dataSources {
        // define the object here, an alternative to loading an 'external' file...
        fileUpload {
            id = "${variables.baseId}_inclusion_list"
            connector = "lucid.fileupload"
            type = "fileupload"
            pipeline = "${variables.baseId}_IPL"
            parserId = "_system"
            properties {
                collection = "${variables.taName}"
                fileId = "${variables.taName}/Typeahead_inclusion_list.csv"
                mediaType = "text/csv"
            }
        }
    }


}

transforms {
    delete = [
            '$..updates',
    ]
    set = [
            [path: '$'],
    ]
}


//indexJson = new File('src/test/resources/components/typeahead/indexpipeline.short-test.json').text
//output = new groovy.text.SimpleTemplateEngine().createTemplate(indexJson).make(variables).toString()
//map = new JsonSlurper().parseText(output)


/*

    indexProfiles {

    }

    queryProfiles {

    }

    features {
        // todo -- enable signals on sidecar coll
    }

    tasks {

    }

    sparkJobs {

    }

 */
