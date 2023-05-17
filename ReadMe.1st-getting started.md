# Getting Started tips and suggestions

## Sample Operations
### Deploy / transform
- DC_Large: 
  - 75% done
  - deployDC_Large.groovy and configDeployDCLarge.groovy 
  - outstanding: 
    - move deployDC_Large from script to object, 
    - provide working example of how to call from CLI or gradle
    - import into fusion and look for errors/gaps
    - list of before and after
      - all the source things that needed to be transformed 
- TypeAhead (?)
- yet-to-be-named

### Savvy-Compare

### Explode (?)



There are some dark alleys of outdated code, so be prepared for some tests to be broken or half baked.

## First tests to dive into for general sanity check

### JsonObject 
This is a wrapper/helper class to abstract some of the functionality related to JsonSlurped objects. 
- [JsonObjectTest.groovy](src/test/groovy/com/lucidworks/ps/transform/JsonObjectTest.groovy)

This tests basic functionality of the (custom-built) JsonObject.
This includes basic slurping and serializing, as well as selection, navigation, and manipulation.

The test also points to Spock Datatables, where are an interesting approach to test a wide range of values/conditions. The @unroll annotation helps give more feedback on each item in the data table.


Other items of interest:
- https://github.com/seanoc5/upval/blob/master/src/test/groovy/com/lucidworks/ps/transform/JsonObjectTest.groovy
- https://github.com/seanoc5/upval/blob/master/src/test/groovy/com/lucidworks/ps/transform/JsonObjectTransformerTest.groovy
- https://github.com/seanoc5/upval/blob/master/src/test/groovy/com/lucidworks/ps/transform/JsonObjectTransformeDeployTest.groovy

Along with:
- https://github.com/seanoc5/upval/blob/master/src/main/groovy/deployDC_Large.groovy

Groovy has bundled PicoCli for command line processing (CliBuilder).
Many of the 'test' scripts are in the root source field (like deployDC_Large.groovy above). They will use a wrapper class around CliBuilder. If you run the script without args, you should get a help/usage message showing that params are required.

In my Intellij Idea setup, I run deployDC_Large with the following args:
```
-s"/Users/sean/work/lucidworks/upval/src/test/resources/apps/DC_Large.zip"
-x"/Users/sean/work/lucidworks/upval/out"
-c"/Users/sean/work/lucidworks/upval/src/test/resources/configurations/configDeployDCLarge.groovy"
```
