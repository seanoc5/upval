package configs     // todo -- do we need this package def? just intellij complaining about linting? any added value??
/**
 * @deprecated oder approach, left here for reference
 */

// general purpose config variables
// require a configSlurper.setBinding(bindingMap) of [appName:'MyMainApplicationName'] -- or change config to not use setBinding variable from calling script
appName = "$appName"

// taName: name of side-car collection, and blob folder, part of other element names
// allow overriding default/static value 'typeahead' with a configSlurper.setBinding(bindingMap)  -- optional
taName = "${taName ?: 'typeahead'}"
// groovy configObject is a 'smart map' which allows execution and evaluation--build baseId as a convenience
baseId = "${appName}_${taName}"


// variables section contains replacement of certain values
// todo -- replace this approach with jayway jsonpath transforms,
variables {
    // from foundry component
    ZKHOST = "ZOOKEEPER-0.ZOOKEEPER-headless:2181,ZOOKEEPER-1.ZOOKEEPER-headless:2181,ZOOKEEPER-2.ZOOKEEPER-headless:2181"
    SIGNALS_AGGR_COLL = "SIGNALS_AGGR_COLLECTION"
    FEATURE_NAME = "${ta.featurename}"
    TYPE_FIELD_1 = "TYPE_FIELD_1"
    TYPE_FIELD_2 = "TYPE_FIELD_2"
    APP = "Components"
    COLLECTION = "COLLECTION"
}
// alternative way of writing config values - outside braces, flat path
variables.TYPE_FIELD_3 = 'TYPE_FIELD_3'
variables.TYPE_FIELD_4 = 'TYPE_FIELD_4'
variables.TYPE_FIELD_5 = ''             // TODO check if it makes sense to leave defaults blank (and should be skipped if remain blank)....?

objects {
    blobs {
        // 'serviceLib' label here is only for readability, can be anything (unique)
        serviceLib {
            type = 'file'
            // zk path here
            path = 'lib/index/FusionServiceLib.js'
            // where to get the blob content from -- assuming it will be posted in a blobs api call...
            source = 'src/main/resources/typeahead/FusionServiceLib.js' // todo -- can this pull from a network source?
            // fusionSource = 'http://lucy:6764/api/blobs/lib/index/FusionServiceLib.js'        // todo add code to make this work as alternative to 'source' above
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
        // reference url to call {{furl}}/api/blobs/lib/index/FusionServiceLib.js
    }

    collections {
        // 'typeahead' label here is purely for labeling and readability, the label is not used, but rather the values inside (the map) drive the details of the object
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


    dataSources {
        fileUpload {
            id = "${baseId}_inclusion_list"
            connector = "lucid.fileupload"
            type = "fileupload"
            pipeline = "${baseId}_IPL"
            parserId = "_system"
            properties = {
                collection = "${taName}"
                fileId = "${taName}/Typeahead_inclusion_list.csv"
                mediaType = "text/csv"
            }
        }
    }

}
