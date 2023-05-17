/**
 * @deprecated look at configTypeAheadDeploy
 */
// name of side-car collection, and blob folder, part of other element names
taName = "${taName ?: 'typeahead'}"
appName = "$appName"
baseId = "${appName}_${taName}"
DELETE_EXISTING = true
version: 'taTest 0.1'


transform {
    copy = [
            [from: '${foundry.destination.APP}', to: "${appName}"],
            [from: '${foundry.destination.COLLECTION}', to: "${taName}"],
            [from: '${foundry.FEATURE_NAME}', to: "${taName}"],

            [from: '${foundry.typeahead.ZKHOST}', to: 'myzk-0.myzk-headless:2181,myzk-1.myzk-headless:2181,myzk-2.myzk-headless:2181'],
            [from: '${foundry.destination.SIGNALS_AGGR_COLL}', to: "${appName}_signals_aggr"],
            [from: '${foundry.typeahead.TYPE_FIELD_1}', to: 'category, flattenedCategoryPath_s, categoryUrl_s, categoryImageUrl_s'],
            [from: '${foundry.typeahead.TYPE_FIELD_2}', to: 'brand, flattenedbrandPath_s, brandUrl_s, brandImageUrl_s '],
            [from: '${foundry.typeahead.TYPE_FIELD_3}', to: 'query_orig_s'],
            [from: '${foundry.typeahead.TYPE_FIELD_4}', to: 'author_s'],
            [from: '${foundry.typeahead.TYPE_FIELD_5}', to: 'department_t'],
    ]

    remove = [
            '$..updates',
            '$..properties',
    ]

}

//template {
//    objectsJson: '/Users/sean/work/lucidworks/upval/src/test/resources/components/ta-objects.json'
//}

blobs {
    serviceLib {
        type = 'file'
        path = '/lib/index/FusionServiceLib.js'
        source = '/typeahead/FusionServiceLib.js'
    }
    badWords {
        type = 'file'
        path = "/${taName}/full-list-of-bad-words_csv-file_2018_07_30.csv"
        source = '/typeahead/full-list-of-bad-words_csv-file_2018_07_30.csv'
    }
    inclusionList {
        type = 'file'
        path = "/${taName}/Typeahead_inclusion_list.csv"
        source = '/typeahead/Typeahead_inclusion_list.csv'
    }
    //{{furl}}/api/blobs/lib/index/FusionServiceLib.js
}

collections {
    typeahead {
        id = "${baseId}"
        searchClusterId = "default"
        commitWithin = 10000
        solrParams {
//            name = "${baseId}"
            numSards = 1
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
