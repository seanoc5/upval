
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
