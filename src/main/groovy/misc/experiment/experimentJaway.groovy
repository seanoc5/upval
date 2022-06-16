package misc.experiment

import com.jayway.jsonpath.*
import com.jayway.jsonpath.internal.path.PathCompiler
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
Map document = slurper.parseText(srcJson)
//Map document = Configuration.defaultConfiguration().jsonProvider().parse(srcJson)
String mypath = '$.a.foo'
def afooVal = JsonPath.read(document, mypath)
log.info "a.foo value : $afooVal (should have gotten 'bar')"

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
def fooBar1 = users.foo.bar

users.foo.bar = 'mytest'
log.info "users foo: ${users.foo}"

try {
    def created = looseContext.read(missingPath)
    looseContext
    log.info "Created? $created"
} catch (PathNotFoundException pnfe){
    log.warn "Path not found: $pnfe"
}

