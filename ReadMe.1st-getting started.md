# Getting Started tips and suggestions

# Tests 
This project uses Spock test framework:
https://spockframework.org/spock/docs/2.1/index.html

There are some dark alleys of outdated code, so be prepared for some tests to be broken or half baked.

## First tests to dive into for general sanity check

### JsonObject 
This is a wrapper/helper class to abstract some of the functionality related to JsonSlurped objects. 
- [JsonObjectTest.groovy](src/test/groovy/com/lucidworks/ps/transform/JsonObjectTest.groovy)

This tests basic functionality of the (custom-built) JsonObject.
This includes basic slurping and serializing, as well as selection, navigation, and manipulation.

The test also points to Spock Datatables, where are an interesting approach to test a wide range of values/conditions. The @unroll annotation helps give more feedback on each item in the data table.
