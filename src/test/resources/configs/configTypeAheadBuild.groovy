// ------------------ Variables to configure deplotment -------------------
FEATURE_NAME = "TAFeature"
APP = "MyApp"
COLLECTION = "MyAppColl"
ZKHOST = "myzk-0.myzk-headless=2181,myzk-1.myzk-headless=2181,myzk-2.myzk-headless=2181"
SIGNALS_AGGR_COLL = "${COLLECTION}_signals_aggr"
TYPE_FIELD_1 = 'category, flattenedCategoryPath_s, categoryUrl_s, categoryImageUrl_s'
TYPE_FIELD_2 = 'brand, flattenedbrandPath_s, brandUrl_s, brandImageUrl_s '
//TYPE_FIELD_3 = "undefined3"
//TYPE_FIELD_4 = "undefined4"
//TYPE_FIELD_5 = "undefined5"
baseId = "${APP}_${FEATURE_NAME}"
numShards = 1
replicationFactor = 2
maxShardsPerNode = 2


// ------------------ Objects and deployment configs below -------------------
objects {
    collections {
        sidecar {
            id = "$baseId"
            searchClusterId = 'default'
            commitWithin = 10000
            solrParams {
                name = "${baseId}"
                numShards = numShards
                replicationFactor = replicationFactor
                maxShardsPerNode = maxShardsPerNode
            }
            type = 'DATA'
            metadata = []
        }
    }


    dataSources {
        fileUpload {
            id = "${baseId}_inclusion_list"
            connector = "lucid.fileupload"
            type = "fileupload"
            pipeline = "${baseId}_IPL"
            parserId = "_system"
            properties {
                collection = "${taName}"
                fileId = "${taName}/Typeahead_inclusion_list.csv"
                mediaType = "text/csv"
            }
        }
    }

    queryPipelines {
        signalsHistory {

        }
    }
    queryProfiles {

    }

    indexPipelines {

    }

    indexProfiles {

    }

    features {
        // todo -- enable signals on sidecar coll
    }

    tasks {

    }

    sparkJobs {

    }


}


//objectsJson =new File('/Users/sean/work/lucidworks/upval/src/test/resources/components/ta-objects.json').text
//Map map = new JsonSlurper().parseText(output)
//objects = new JsonSlurper().parse(sourceObjects.toURL())
//println(objects)
