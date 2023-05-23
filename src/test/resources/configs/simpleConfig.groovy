import groovy.json.JsonSlurper

// name of side-car collection, and blob folder, part of other element names
taName = "${taName ?: 'myTypeahead'}"
appName = "${appName ?: 'defaultAppName'}"
baseId = "${appName}_$taName"
username = "${username ?: 'testUser'}"
// can't seem to get access to /resources dynamically? only to /classes/...
rootObjectsUrl = getClass().getResource('/')
println "Resources root url: $rootObjectsUrl"
fooFile = new File(rootObjectsUrl.toURI())
println("Resources root file path: ${fooFile.absolutePath}")

objectsUrl = getClass().getResource('/components/simpleObjects.json')
println objectsUrl

//sourceObjects = new File(objectsUrl)
//sourceObjects = new File('/home/sean/work/lucidworks/upval/src/test/resources/components/simpleObjects.json')
objects = new JsonSlurper().parse(objectsUrl)
//objects = new JsonSlurper().parse(sourceObjects.toURL())
//println(objects)


test {
    foo = "Testing eval: ${baseId}"
    mydate = new Date()
}

