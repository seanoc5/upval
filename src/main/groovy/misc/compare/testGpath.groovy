package misc.compare

import groovy.json.JsonSlurper
import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name)
// found somewhere like: https://stackoverflow.com/questions/46551949/how-to-get-key-from-arraylist-nested-in-json-using-groovy-and-change-its-value

String src = '''{
  "source": "source",
  "orderId": null,
  "Version": null,
  "quote": {
    "globalTransactionId": "k2o4-6969-1fie-poef",
    "quoteStatus": "Not Uploaded",
    "attributes": [
      {
        "appliedFlag": "Y",
        "attributeDetail": {
          "name": "name1",
          "value": "value1"
        },
        "attributeName": "attribute1"
      },
      {
        "appliedFlag": "N",
        "attributeDetail": {
          "name": "name2",
          "value": "value2"
        },
        "attributeName": "attribute2"
      }
    ]}
  }'''

Map<String, Object> json = new JsonSlurper().parseText(src)

//def quote = json.quote
def quoteStatus = json.quote.quoteStatus
quoteStatus = 'updated quote value'
String jpath = 'quote.quoteStatus'
List<String> pathParts = jpath.split('\\.')
pathParts.each {

}
json[pathParts[0]][pathParts[1]]= quoteStatus
// -------test 2 ---------
def flatten2 = flatten(json)
log.info "Flatten 2: $flatten2"


def bazz = flattenMap(json)
String path = 'orderId'
//String path = 'quote.quoteStatus'
bazz[path]= 'my updated value'
// sample code to show reading a path, then set it (null) and show update-- via eval of gpath string
path = 'ROOT.quote.attributes[0].attributeDetail.name'
var foo = jsonx(json, path)
log.info "Foo: $foo -- from path: $path"
jsonx(json, 'ROOT.quote.attributes[0].attributeDetail.name = null')
println jsonx(json, 'ROOT.quote.attributes[0].attributeDetail.name')


def jsonx(Object json, String expr) {
    return Eval.me('ROOT', json, expr)
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
         if (v instanceof Map){
             flatten(v, separator).collectEntries { q, r ->
                 [(k + separator + q): r]
             }
         }
         else {
             [(k):v]
         }
     }
}
