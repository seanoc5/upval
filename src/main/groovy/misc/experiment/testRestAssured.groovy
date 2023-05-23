//package misc.experiment
//
//import groovy.json.JsonSlurper
//import io.restassured.path.json.JsonPath
//import org.apache.log4j.Logger
///**
// * Script exploring RestAssured functionality
// */
//
//Logger log = Logger.getLogger(this.class.name);
//log.info "Starting script ${this.class.name}..."
//
//JsonSlurper jsonSlurper = new JsonSlurper()
//File srcJsonFile = new File("/home/sean/work/lucidworks/upval/src/main/resources/examples/F4.ldap-acls.example.json")
//def srcJson = jsonSlurper.parse(srcJsonFile)
//
//JsonPath raPath = JsonPath.with(srcJsonFile)
//String path1 = "pipeline"
//def bar = raPath.get(path1)
//bar == 'testpipeline'
//log.info "Done...?"
