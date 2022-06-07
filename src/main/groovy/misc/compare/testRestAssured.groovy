package misc.compare

import groovy.json.JsonSlurper
import io.restassured.path.json.JsonPath
import org.apache.log4j.Logger

/**
 * Script to compare two (json origin) objects
 */

Logger log = Logger.getLogger(this.class.name);

log.info "Starting script ${this.class.name}..."

JsonSlurper jsonSlurper = new JsonSlurper()
File srcJsonFile = new File("/home/sean/work/lucidworks/upval/src/main/resources/examples/F4.ldap-acls.example.json")
def srcJson = jsonSlurper.parse(srcJsonFile)

Map map = [
        top1: [
                middle1a: [bottom1a1: [subbottom1a1a: 'endleaf1'], bottom1a2: 'endleaf2',],
                middle1b: [bottom1b1: 'endleaf3'],
        ],
        top2: [
                middle2a: [bottom2a1: 'endleaf1', bottom2a2: 'endleaf2'],
                middle2b: [bottom2b1: 'endleaf3']
        ]
]

//def f1 = map.'**'.find{ it -> true }

JsonPath raPath = JsonPath.with(srcJsonFile)
String path1 = "pipeline"
def bar = raPath.get(path1)
bar = 'testpipeline'
log.info "Done...?"
