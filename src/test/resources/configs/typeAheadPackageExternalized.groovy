package configs

import com.jayway.jsonpath.JsonPath
import com.lucidworks.ps.transform.JsonObject
import groovy.json.JsonSlurper

// --------------------- variables defined below :: customize most/all of these variables ----------------------
variables {
    FEATURE_NAME = 'ExtTA'
    APP = "ExtApp"
    COLLECTION = APP
    ZKHOST = "myzk-0.myzk-headless=2181,myzk-1.myzk-headless=2181,myzk-2.myzk-headless=2181"
    SIGNALS_AGGR_COLL = "${APP}_signals_aggr"
    TYPE_FIELD_1 = 'brand, flattenedbrandPath_s, brandUrl_s, brandImageUrl_s '
    TYPE_FIELD_2 = "title, mytitle_s, doc_url_s, icon_url_s"
    numShards = 1
    // created these variables to "highlight" they are worth reviewing and adjusting as desired
    replicationFactor = 2
    maxShardsPerNode = 1
    baseId = "${APP}_${FEATURE_NAME}"                           // created shortcut since this is so common in source package
    q = '$q'                                                    // quick hack to avoid escaping `$q`
}
// --------------------- variables defined above ----------------------


// ------------------ Helper utils -------------------
// helper variable to streamline converting json text into Object, called below on template outputs
slurper = new JsonSlurper()
engine = new groovy.text.SimpleTemplateEngine()


// ------------------ Templates work below  -------------------
templateDir = new File('./src/test/resources/components/typeahead')

// one-liner approach with URL rather than file
collection = engine.createTemplate(new URL('file:./src/test/resources/components/typeahead/collection.sidecar.v1.json')).make(variables).toString()

// less shorthand, create file, and then perform templating
qrypTemplate = new File(templateDir, 'querypipeline.main.v1.json').text
qryp = engine.createTemplate(qrypTemplate).make(variables).toString()

// semi-advanced: define 'jsUnwantedTerms' as the escaped javascript from a file, then templatize that into the indexpipeline source :: todo -- check variable expansion in js
jsUnwantedTerms = JsonObject.escapeSource(new File(templateDir,'excludeUnwantedTerms.js').text)             // load long javascript, escape it, any then template it in to the index pipeline
idxpTemplate = new File(templateDir,'/indexpipeline.main.v1.json').text
idxp = engine.createTemplate(idxpTemplate).make(variables).toString()

dsrc = engine.createTemplate(new File(templateDir, 'datasource.fileupload.v1.json')).make(variables).toString()

// this should be a list of maps, so slightly different from single map objects referenced above ^^^
blbs = engine.createTemplate(new File(templateDir, 'blobs.various-ta.v1.json')).make(variables).toString()


// --------------------- template for objects.json below ---------------------
objects {
    // these are arrays of maps, square brackets in groovy can be List [a,b,c] or Map [foo: 'a', bar: 'b', bizz:'c'], slurper converts qryp template output (String) into a JsonObject (Maps and Collections combined)
    queryPipelines = [
            slurper.parseText(qryp)         // this should be a map of the one query pipeline, added inside an array
    ]

    // shorthand version of multi-line def above
    indexPipelines = [slurper.parseText(idxp)]

    collections = [slurper.parseText(collection)]

    dataSources = [slurper.parseText(dsrc)]

    // slightly different formatting/approach, template is a list of blob maps, showing how we can handle that type of templating here
    blobs = slurper.parseText(blbs)

    // defining the remaining elements "in-line", these could easily be externalized as well, leaving here for reference
    features = [
            ["name": "partitionByTime", "collectionId": "${baseId}", "params": {}, "enabled": false],
            ["name": "signals", "collectionId": "${baseId}", "params": {}, "enabled": true]
    ]

    queryProfiles = [
            [
                    "id"           : "${baseId}_QPF",
                    "queryPipeline": "${baseId}_QPL",
                    "searchHandler": "select",
                    "searchMode"   : "all",
                    "collection"   : "${baseId}",
            ],
            [
                    "id"           : "${baseId}_entity_QPF",
                    "queryPipeline": "${baseId}_QPL",
                    "searchHandler": "select",
                    "searchMode"   : "all",
                    "collection"   : "${baseId}",
//                    "additionalProperties": {},
                    "params"       : [
                            "key"   : "entityOnly",
                            "value" : "true",
                            "policy": "append"
                    ]
            ],
            [
                    "id"           : "${baseId}_history_QPF",
                    "queryPipeline": "${baseId}_QPL",
                    "searchHandler": "select",
                    "searchMode"   : "all",
                    "collection"   : "${baseId}",
                    "params"       : [
                            "key"   : "historyOnly",
                            "value" : "true",
                            "policy": "append"
                    ]
            ]
    ]

    tasks = [
            [
                    "type"      : "rest-call",
                    "id"        : "${baseId}_docs_cull",
                    "callParams": [
                            "uri"   : "solr://${baseId}/update",
                            "method": "post",
                            "entity": "<root><delete><query>ta_type:history AND (last_updated_tdt:[* TO NOW-10MINUTES] OR (*:* AND -last_updated_tdt:[* TO *]))</query></delete><commit/></root>"
                    ],
            ]
    ]



    sparkJobs = [
            [
                    "id"                          : "${baseId}_clicks_load",
                    "cacheAfterRead"              : false,
                    "continueAfterFailure"        : false,
                    "defineFieldsUsingInputSchema": true,
                    "atomicUpdates"               : false,
                    "format"                      : "solr",
                    "transformSql"                : "SELECT query_s AS value_s,\n  SUM(aggr_count_i) AS signal_count_i,\n  current_timestamp() AS last_updated_tdt,\n  AVG(weight_d) AS popularity_d\nFROM _input\nGROUP BY value_s",
                    "readOptions"                 : [
                            ["key": "collection", "value": "${SIGNALS_AGGR_COLL}"],
                            ["key": "zkHost", "value": "${ZKHOST}"]
                    ],
                    "clearDatasource"             : false,
                    "outputIndexPipeline"         : "${baseId}_IPL",
                    "type"                        : "parallel-bulk-loader",
                    "outputCollection"            : "${baseId}"

            ],
            [
                    "id"                          : "${baseId}_entity_load",
                    "cacheAfterRead"              : false,
                    "atomicUpdates"               : false,
                    "format"                      : "solr",
                    "outputIndexPipeline"         : "${baseId}_IPL",
                    "type"                        : "parallel-bulk-loader",
                    "outputCollection"            : "${baseId}",
                    "transformScala"              : "import com.lucidworks.spark.util.SolrSupport\nimport java.time.LocalDateTime\nimport java.time.format.DateTimeFormatter\nimport java.time.ZoneOffset\nimport org.apache.spark.sql.types.{\n    StructType, StructField, StringType}\nimport org.apache.spark.sql.Row\nimport org.apache.spark.sql.Column\nimport scala.util.control._\n\n\n// main PBL transform\ndef transform(inputDF: Dataset[Row]) : Dataset[Row] = {\n    println(\"---BEGIN TYPE-BASED DOCUMENT LOAD---\")\n\n    var outputDF = spark.emptyDataFrame\n\n    var counter = 1\n    var currentTypeField =  \"\"\n    val loop = new Breaks;\n    // Loop through spark settings for the Type fields which should be in spark.typeField_* format\n    loop.breakable {\n        do {\n            var boolCheck = false\n            try\n            {\n                currentTypeField = sc.getConf.get(\"spark.typeField_\" + counter)\n                counter += 1\n            }\n            catch\n            { \n                case x: NoSuchElementException => \n                { \n                    println(\"End of type field arguments.\")\n                    boolCheck = true\n                }\n            }\n            if(boolCheck){ loop.break }\n\n            println(\"CURRENT TYPE: \" + currentTypeField)\n            //Type fields should be in format: {type name}, {value field}, [{Any other additional fields to pull into the suggestion document separated by \", \"}]\n            val typeFieldArray = currentTypeField.split(\",\").map(_.trim)\n            var fieldArray: Array[Column] = new Array[Column](typeFieldArray.length + 1)\n\n            //Field names wrapped in ` to escape characters like .\n            var typeName = typeFieldArray(0)\n            var valueField = \"`\" + typeFieldArray(1) + \"`\"\n            //Define columns\n            var idCol = regexp_replace(concat(lit(typeName + \"_\"),col(valueField)),\"\\\\s+\",\"\").as(\"id\")\n            var valueCol = col(valueField).as(\"value_s\")\n            var typeCol = lit(typeName).as(\"type\")\n\n            fieldArray(0) = idCol\n            fieldArray(1) =  valueCol            \n            fieldArray(2) =  typeCol\n\n            //Add all other additional fields\n            for(fieldIndex <- 2 until typeFieldArray.length){\n                var additionalField = typeFieldArray(fieldIndex)\n                fieldArray(fieldIndex+1) = col(\"`\" + additionalField + \"`\").as(additionalField)\n            }\n\n\n            //Handle optional fields and build documents for the current type name\n            var currentDF = spark.emptyDataFrame\n            currentDF = inputDF.select(fieldArray: _*).distinct()\n\n            //combine the current type dataframe with the final output dataframe\n            val cols1 = outputDF.columns.toSet\n            val cols2 = currentDF.columns.toSet\n            val total = cols1 ++ cols2 // union\n\n            def expr(myCols: Set[String], allCols: Set[String]) = {\n                allCols.toList.map(x => x match {\n                    case x if myCols.contains(x) => col(x)\n                    case _ => lit(null).as(x)\n                })\n            }\n\n            outputDF = outputDF.select(expr(cols1, total):_*).union(currentDF.select(expr(cols2, total):_*))\n            println(outputDF.head())\n        }\n        while(currentTypeField != null)\n    }\n\n    println(\"---END TYPE-BASED DOCUMENT LOAD---\")\n    println(outputDF.count() + \" DOCUMENTS WRITTEN\")\n\n    outputDF.withColumn(\"indexed_date\", lit(LocalDateTime.now(ZoneOffset.UTC)+\"Z\"))\n    .withColumn(\"ta_type\", lit(\"entity\"))\n}",
                    "sparkConfig"                 : [
                            ["key": "spark.sql.caseSensitive", "value": "true"],
                            ["key": "spark.typeField_1", "value": "${TYPE_FIELD_1}"],
                            ["key": "spark.typeField_2", "value": "${TYPE_FIELD_2}"],
/*
// uncomment here and above as desired
                                                     [
                                                             "key"  : "spark.typeField_3",
                                                             "value": "${TYPE_FIELD_3}"
                                                     ],
                                                     [
                                                             "key"  : "spark.typeField_4",
                                                             "value": "${TYPE_FIELD_4}"
                                                     ],
                                                     [
                                                             "key"  : "spark.typeField_5",
                                                             "value": "${TYPE_FIELD_5}"
                                                     ]
*/
                    ],
                    "continueAfterFailure"        : false,
                    "defineFieldsUsingInputSchema": true,
                    "readOptions"                 : [
                            ["key": "collection", "value": "${COLLECTION}"],
                            ["key": "zkHost", "value": "${ZKHOST}"]
                    ],
                    "clearDatasource"             : true
            ]
    ]

}


// optional?? remove this?
metadata {
    formatVersion = "1"
    exportedBy = "admin"
    exportedDate = "2021-07-09T22:28:12.910Z"
    fusionVersion = "5.4.0"
    fusionGuid = "d62d4466-a46e-4948-97b4-58597712cc7e"
}

// skip properties as well?
// properties {}


//transformations done here via config slurper evaluation
// use jayway context to manipulate `objects` built via configuration above
jsonContext = JsonPath.parse(objects)
// clean up unwanted things (optional: shows how to call jayway in the config file)
//jsonContext.delete('$..updates')
jsonContext.set('$.datasources[0].id', 'myfoo')

// set(replace) customized names for various elements based on variables above
// Package developer "knows" what things need to be set, these rules are customizable, but represent "typical" changes
//jsonContext.set('$.dataSources.fileUpload.id', baseId)
//jsonContext.set('$.dataSources.fileUpload.properties.collection', baseId)

/*
transforms {
    all {
        delete = [
                '$..updates',
        ]
        set = [
                [path: '$'],
        ]
    }
}
*/


