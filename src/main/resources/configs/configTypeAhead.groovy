// name of side-car collection, and blob folder, part of other element names
taName = "${taName ?: 'typeahead'}"
appName = "$appName"
baseId = "${appName}_$taName"
DELETE_EXISTING = true


transform {
    copy = [
            [sourcePath     : '.*', sourceItemPattern: 'LWF_',
             destinationPath: '', destinationExpression: 'Acme_'],
    ]


}

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
