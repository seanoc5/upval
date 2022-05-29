# Mapping Notes

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

