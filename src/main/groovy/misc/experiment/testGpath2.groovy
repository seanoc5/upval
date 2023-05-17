package misc.experiment

import org.apache.log4j.Logger

@groovy.transform.Field
Logger log = Logger.getLogger(this.class.name)
log.info "Start script ${this.class.simpleName}"

Map srcMap = [
        one: [
                aMap: [1, 2],
                bSubMap: [b1: 'b-one', b2: 'b-two', b3list: ['b3-1', 'b3-1']],
                clistOfMaps: [[cSubmap1:[1,2]], [csubMap2:[3,4]]]],
        two:[1,2,3]
]

assert srcMap.one.aMap[0] == 1
assert srcMap['one']['aMap'][1] == 2
assert srcMap['one']['bSubMap']['b1'] == 'b-one'
assert srcMap['one']['bSubMap']['b3list'][0] == 'b3-1'

// these don't work properly -- not handling lists...
def flattenSimple = flatten(srcMap)
def bazz = flattenMap(srcMap)

String getPath = 'ROOT.one.aMap[1]'
var foo = evalObjectPathExpression(srcMap, getPath)
assert foo == 2

String setExpression = 'ROOT.one.aMap[1]=88'
var bar = evalObjectPathExpression(srcMap, setExpression)
assert bar == 88

log.info "Done...?"



def evalObjectPathExpression(Object o, String expr, String root = 'ROOT') {
    String pathExpression = expr
    if(expr.startsWith('/')){
        log.info "Converting slashy expression ($expr) to gpath-like expression for groovy Eval.me -- starts with 'root[${root}].'"
        List<String> parts = expr.split('/')
        if(parts[0] == ''){
            log.info "set start of path expression to root: $root..."
            parts[0] = root
        }
        pathExpression = parts.join('.')
    } else{
        log.debug "Passed in propert Eval.me expression (does not start with slash '/', no change needed: '$expr'..."
    }

    def result = Eval.me('ROOT', o, pathExpression)
    return result
}


// https://stackoverflow.com/questions/51288137/flattening-a-nested-map-in-groovy
String concatenate(String k, String v) {
    "${k}.${v}"
}

def flattenMap(Map map) {
    map.collectEntries { k, v ->
        v instanceof Map ?
                flattenMap(v).collectEntries { k1, v1 ->
                    String key = concatenate(k, k1)
                    [(key): v1]
                } :
                [(k): v]
    }
}


Map flatten(Map m, String separator = '.') {
    m.collectEntries { k, v ->
        if (v instanceof Map) {
            flatten(v, separator).collectEntries { q, r ->
                [(k + separator + q): r]
            }
        } else {
            [(k): v]
        }
    }
}
