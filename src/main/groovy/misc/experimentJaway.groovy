package misc

import com.jayway.jsonpath.*
import com.jayway.jsonpath.internal.path.PathCompiler
import groovy.json.JsonSlurper
import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name);

String srcJson = '''
{
    "a": {
        "foo": "bar"
    },
    "b": {
        "bizz" : "buzz"
    }
}'''

// -------------------- Default config ------------------------
Map document = new JsonSlurper().parseText(srcJson)
//Map document = Configuration.defaultConfiguration().jsonProvider().parse(srcJson)
String mypath = '$.a.foo'
def foo = JsonPath.read(document, mypath)
log.info "foo : $foo"

String newfoo = 'Sean'
DocumentContext context = JsonPath.parse(document)
def bar = context.set(mypath, newfoo)
String updatedFoo = JsonPath.read(document, mypath)
log.info " Updated bar : $updatedFoo"

String missingPath = '$.mynew.bar'
try {
    def missingBar = JsonPath.read(document, missingPath)
    log.info "Found missing bar???"
} catch (PathNotFoundException pnfe){
    log.warn "Path does not exist: $missingPath -- $pnfe"
}

// -------------------- LOOSE config ------------------------
Configuration looseConfig = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL).build();
def compiledMissing = PathCompiler.compile(missingPath);

DocumentContext looseContext = JsonPath.using(looseConfig).parse(document)
def myAdd = looseContext.add(missingPath, 'CreatedNowValue')
log.info "My add: $myAdd"


def tree
tree = { -> return [:].withDefault{ tree() } }

def users = tree()
users.harold.username = 'hrldcpr'
users.yates.username = 'tim'
def bizz = users.foo.bar

users.foo.bar = 'mytest'
log.info "users foo: ${users.foo}"

try {
    def created = looseContext.read(missingPath)
    looseContext
    log.info "Created? $created"
} catch (PathNotFoundException pnfe){
    log.warn "Path not found: $pnfe"
}

