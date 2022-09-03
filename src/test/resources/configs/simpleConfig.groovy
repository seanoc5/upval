import groovy.json.JsonSlurper

// name of side-car collection, and blob folder, part of other element names
taName = "${taName ?: 'myTypeahead'}"
appName = "${appName ?: 'defaultAppName'}"
baseId = "${appName}_$taName"
username = "${username ?: 'testUser'}"
// can't seem to get access to /resources dynamically? only to /classes/...
objectsUrl = getClass().getResource('/')
println "Resources root url: $objectsUrl"
fooFile = new File(objectsUrl.toURI())
println("Resources root file path: ${fooFile.absolutePath}")

sourceObjects = new File('/home/sean/work/lucidworks/upval/src/test/resources/components/simpleObjects.json')
objects = new JsonSlurper().parse(sourceObjects.toURL())
//println(objects)


test {
    foo = "Testing eval: ${baseId}"
    mydate = new Date()
}

