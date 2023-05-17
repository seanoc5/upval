import com.lucidworks.ps.transform.JsonObject

variables {
    FEATURE_NAME = "MyTypeAhead"
    APP = "MyApp"
    COLLECTION = "MyAppColl"
    ZKHOST = "myzk-0.myzk-headless=2181,myzk-1.myzk-headless=2181,myzk-2.myzk-headless=2181"
    SIGNALS_AGGR_COLL = "SIGNALS_AGGR_COLLECTION"
    TYPE_FIELD_1 = "TYPE_FIELD_1"
    TYPE_FIELD_2 = "TYPE_FIELD_2"
//    TYPE_FIELD_3= "TYPE_FIELD_3"
//    TYPE_FIELD_4= "TYPE_FIELD_4"
//    TYPE_FIELD_5= "TYPE_FIELD_5"
    baseId = "${APP}_${FEATURE_NAME}"
    jsSource = new File('src/test/resources/components/typeahead/excludeUnwantedTerms.js').text
//    println "${JsonObject.escapeSource(jsSource)}"
    jsUnwantedTerms = JsonObject.escapeSource(jsSource)

    jsSource = """ function (doc){ return doc; } """
    regexNormalize = 'regex-here'
//    regexNormalize = '/[`~!@#$^*()|+:,<>\{\}\[\]\/]/gi'
    q = '$q'

}
//testFile = new File('.')
//println("Running from: ${testFile.absolutePath}")
//println "Variables: ${variables.collect{"\n\t\t$it"}}"

//indexJson = new File('src/test/resources/components/typeahead/indexpipeline.short-test.json').text
indexJson = new File('src/test/resources/components/typeahead/ta-objects.test.json')
println indexJson.absolutePath + " exists? " + indexJson.exists()
//output = new groovy.text.SimpleTemplateEngine().createTemplate(indexJson.text).make(variables).toString()
//map = new JsonSlurper().parseText(output)
