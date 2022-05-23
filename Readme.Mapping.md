

## Links and background docs
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

https://readlearncode.com/java-ee/java-ee-8-json-processing-1-1-json-patch-overview/

https://readlearncode.com/java-ee/java-ee-8-json-processing-1-1-json-merge-patch-overview/

https://gregsdennis.github.io/Manatee.Json/usage/pointer.html

https://stackoverflow.com/questions/1449001/is-there-a-java-utility-to-do-a-deep-comparison-of-two-objects

https://assertj.github.io/doc/#basic-usage


