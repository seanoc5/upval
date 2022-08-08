package configs

// name of side-car collection, and blob folder, part of other element names
ta_name = 'myTypeAhead'

blobs {
    serviceLib {
        path = 'lib/index/FusionServiceLib.js'
        source = 'src/main/resources/typeahead/FusionServiceLib.js'
    }
    badWords {
        path = "${ta_name}/full-list-of-bad-words_csv-file_2018_07_30.csv"
        source = 'src/main/resources/typeahead/full-list-of-bad-words_csv-file_2018_07_30.csv'
    }
}

//index {
//    pipeline {
//
//    }
//}
