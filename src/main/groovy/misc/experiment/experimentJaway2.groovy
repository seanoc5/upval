package misc.experiment

import com.jayway.jsonpath.*
import com.jayway.jsonpath.internal.path.PathCompiler
import groovy.json.JsonSlurper
import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name);

JsonSlurper slurper = new JsonSlurper()
File srcFile = new File('/home/sean/work/lucidworks/upval/src/main/resources/templates/f4.ldap-acls.json')       // todo -- replace with loading resource/relative path
Map srcMap = slurper.parse(srcFile)
log.info "Source Map: $srcMap"

File destFile = new File('/home/sean/work/lucidworks/upval/src/main/resources/templates/F5.ldap-acls.json')       // todo -- replace with loading resource/relative path
Map destMap = slurper.parse(srcFile)

Map rulesMap = [
        transformerClass: "SimlpeTransform",            // skip transformer class for the moment
        set             : [
                '$.type'     : 'lucidworks.ldap',
                '$.connector': 'lucidworks.ldap'
        ],
        copy            : [
                '$.id'      : '$.id',
                '$.pipeline': '$.pipeline',
                '$.parserId': '$.parserId',
//                        '$.properties/searchProperties/userSearchProp/userFilter' : '/properties/f.ldap_user_filter',
//                        '/properties/searchProperties/groupSearchProp/userFilter': '/properties/f.ldap_group_filter'
        ]
]
DocumentContext srcContext = JsonPath.parse(srcMap)
DocumentContext destContext = JsonPath.parse(destMap)


rulesMap.set.each { String path, String ruleValue ->
    def oldValue = destContext.read(path)
    log.info "Setting value ($ruleValue) from rules mapping for dest path: $path  -- old Value:'$oldValue'"
    destContext.set(path, ruleValue)
}
log.info "After set rules: $destMap"


rulesMap.copy.each { String path, String ruleValue ->
    def oldVal = JsonPath.read(srcMap, path)
    destContext.set(ruleValue, oldVal)
    log.info "Copied value($oldVal) from source Path: $path to dest/ruleP : $afooVal (should have gotten 'bar')"
}

String newAfooVal = 'Sean'
DocumentContext context = JsonPath.parse(document)
DocumentContext context1 = context.set(mypath, newAfooVal)
String updatedFoo = JsonPath.read(document, mypath)
log.info " Updated value after write: $updatedFoo (should be '$newAfooVal')"

// todo -- this needs code to fill in 'missing' elements like `mynew`
String missingPath = '$.mynew.bar'
try {
    def missingBar = JsonPath.read(document, missingPath)
    log.info "Found missing bar???"
} catch (PathNotFoundException pnfe) {
    log.warn "Path does not exist: $missingPath -- $pnfe"
}

// -------------------- LOOSE config ------------------------
Configuration looseConfig = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL).build();
def compiledMissing = PathCompiler.compile(missingPath);

DocumentContext looseContext = JsonPath.using(looseConfig).parse(document)
def myAdd = looseContext.add(missingPath, 'CreatedNowValue')
log.info "My add: $myAdd"


def tree
tree = { -> return [:].withDefault { tree() } }

def users = tree()
users.harold.username = 'hrldcpr'
users.yates.username = 'tim'
def fooBar1 = users.foo.bar

users.foo.bar = 'mytest'
log.info "users foo: ${users.foo}"

try {
    def created = looseContext.read(missingPath)
    looseContext
    log.info "Created? $created"
} catch (PathNotFoundException pnfe) {
    log.warn "Path not found: $pnfe"
}

