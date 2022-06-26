# Mapping Notes

## Transform Overview
### Helper.flattenWithLeafObject approach
- flatten slurped json object -> get Map of all element flat-paths with values/objects 
- iterate all copyRules
  - evaluate source (one or more matching sources)
  - each source flat-path entry that matches copyRule source pattern:
    - get dest path (if source path has regex group, build dest path with substitutions)
    - copy value from source entry (ies) to dest entry 

## Objects needing Transformation
### Ldap-Acls
f.userPrincipal ->


## Links and background docs

### Jayway
https://github.com/json-path/JsonPath

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

