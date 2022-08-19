import groovy.json.JsonSlurper

variables {
    ZKHOST: "myzk-0.myzk-headless:2181,myzk-1.myzk-headless:2181,myzk-2.myzk-headless:2181"
    SIGNALS_AGGR_COLL: "SIGNALS_AGGR_COLLECTION"
    FEATURE_NAME: "TYPEAHEAD_DW"
    TYPE_FIELD_1: "TYPE_FIELD_1"
    TYPE_FIELD_2: "TYPE_FIELD_2"
    TYPE_FIELD_3: "TYPE_FIELD_3"
    TYPE_FIELD_4: "TYPE_FIELD_4"
    TYPE_FIELD_5: "TYPE_FIELD_5"
    APP: "Components"
    COLLECTION: "COLLECTION"
}

objectsJson =new File('/Users/sean/work/lucidworks/upval/src/test/resources/components/ta-objects.json').text
String output =  new groovy.text.SimpleTemplateEngine().createTemplate(objectsJson).make(variables).toString()
Map map = new JsonSlurper().parseText(output)


//objects = new JsonSlurper().parse(sourceObjects.toURL())
//println(objects)

