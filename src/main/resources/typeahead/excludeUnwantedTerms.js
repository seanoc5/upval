(function () {
    'use strict';
    var FusionServiceLib;

    var Pattern = java.util.regex.Pattern;
    var Matcher = java.util.regex.Matcher;

    var VALUE_FIELD = "value_s";
    var TYPE_FIELD = "ta_type";
    var EXCLUDE_BLOB = "${FEATURE_NAME}/full-list-of-bad-words_csv-file_2018_07_30.csv";

    //Global to hold the NSFW words
    var exclusionList;


    return function (doc, ctx) {
        if (doc.getId() == null || doc.getFirstField(TYPE_FIELD) == null) {
            return doc;
        }

        if (!doc.getFirstField(TYPE_FIELD).getValue().toString() == "entity") {
            return doc;
        }

        var exclude = false;

        // Load in lucidworks.ps FusionService library
        if (null == FusionServiceLib) {
            FusionServiceLib = ctx.FusionServiceLib;
        }

        if (doc.hasField(VALUE_FIELD)) {
            if (!exclusionList) {
                var response = FusionServiceLib.getStringBlob(EXCLUDE_BLOB);
                if (response) {
                    exclusionList = response.trim().split("\n");
                }
            }

            var field = doc.getFirstField(VALUE_FIELD);
            var val = field.getValue().toString();


            for (var i = 0; i < exclusionList.length && !exclude; i++) {
                var line = exclusionList[i];
                var offset = line.indexOf(';');
                if (offset > 0) {
                    line = line.substr(0, offset);
                }

                //Regex: If profane word/phrase matches an individual word in value, remove
                //Match: apple profane-word orange
                //No match: appleprofane-wordorange
                var pattern = Pattern.compile("(?<!\\w)" + line + "(?!\\w)", Pattern.CASE_INSENSITIVE);
                var matcher = pattern.matcher(val);

                exclude = matcher.find();
            }
        }

        if (exclude) {
            return;
        } else {
            return doc;
        }
    }


})();
