# Mapping Notes

## Transform Overview
### JsonObject.flattenWithLeafObject approach
- flatten slurped json object -> get Map of all element flat-paths with values/objects 
- iterate all copyRules
  - evaluate source (one or more matching sources)
  - each source flat-path entry that matches copyRule source pattern:
    - get dest path (if source path has regex group, build dest path with substitutions)
    - copy value from source entry (ies) to dest entry 

### JsonObject set value
* /a/b/foo = bar
  * a exists
    * b exists
      * set (create or overwrite) foo
    * b missing
      * create b entry (blank)
      * set (create or overwrite) foo
* /a/0/b/foo
  * a exists



## Objects needing Transformation
### Ldap-Acls
f.userPrincipal ->


## Links and background docs

### Jayway
https://github.com/json-path/JsonPath

#### Syntax
it seems jayway may not be able to select "any element containing value 'foo'":  
https://stackoverflow.com/questions/62809678/filter-by-values-where-key-is-unknown  

other links:  
https://stackoverflow.com/questions/53277865/what-is-the-java-library-to-remove-modify-a-json-object-based-on-json-path-or-h  
https://stackoverflow.com/questions/68436665/jsonpath-how-to-get-the-whole-tree-except-one-node  
https://jsonnet.org/

**Various Examples:** \
http://jsonpath.herokuapp.com/?path=$..book[?(@.price%20%3C=%20$[%27expensive%27])]
`$..book[?(@.author =~ /Herman.*/)]`

**Set values:**  
https://github.com/json-path/JsonPath#set-a-value

**Remove**: \
https://stackoverflow.com/questions/56092846/jsonpath-removing-object-from-a-complete-object


### Various

https://datatracker.ietf.org/doc/html/rfc6901

https://www.javacodegeeks.com/2018/04/get-to-know-json-pointer-json-p-1-1-overview-series.html
```
<dependency>
    <groupId>javax.json</groupId>
    <artifactId>javax.json-api</artifactId>
    <version>1.1</version>
</dependency>
 
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>javax.json</artifactId>
    <version>1.1</version>
</dependency>
```

`JsonValue jsonValue = Json.createPointer("/title").getValue(jsonObject);`

`JsonObject jsonObject = Json .createPointer("/category") .add(jsonObject, Json.createValue("Programming"));`

https://stackoverflow.com/questions/4085353/comparing-two-collections-in-java

https://readlearncode.com/java-ee/java-ee-8-json-processing-1-1-json-patch-overview/

https://readlearncode.com/java-ee/java-ee-8-json-processing-1-1-json-merge-patch-overview/

https://gregsdennis.github.io/Manatee.Json/usage/pointer.html

https://stackoverflow.com/questions/1449001/is-there-a-java-utility-to-do-a-deep-comparison-of-two-objects
https://stackoverflow.com/questions/50967015/how-to-compare-json-documents-and-return-the-differences-with-jackson-or-gson/50969020#50969020


https://assertj.github.io/doc/#basic-usage
https://www.baeldung.com/java-compare-hashmaps
https://guava.dev/releases/20.0/api/docs/com/google/common/collect/MapDifference.html
https://www.baeldung.com/jackson-compare-two-json-objects
https://stackoverflow.com/questions/2253750/testing-two-json-objects-for-equality-ignoring-child-order-in-java
https://www.javacodegeeks.com/2021/05/json-patch-and-json-merge-patch-in-java.html
https://javaee.github.io/jsonp/getting-started.html
https://blogs.sap.com/2022/05/26/gpath-for-json-in-cloud-integration/



/*
    def "should add a missing node via groovy map.withDefault and inject"(){
        given:
        // https://stackoverflow.com/questions/56683855/groovy-map-populate-with-default-element
        def result = [1,2,3,4].inject([:].withDefault{[]}){ m, i ->
            m[ i%2==0 ? 'odd' : 'even' ] << i
            m
        }
        // => [even:[1, 3], odd:[2, 4]]
    }
*/


## Notes
### JsonOutput (customization)
`// http://man.hubwiz.com/docset/Groovy.docset/Contents/Resources/Documents/groovy/json/JsonGenerator.Options.html
def jsonDefaultOutput = new JsonGenerator.Options()
        .dateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .build()
`

### Transform rules
#### Copy
[sourcePath:'(.*Leaf)', sourceItem:'(simple)(.*)', destinationPath:'', destinationValue:'']
