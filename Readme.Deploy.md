# Deploy Component (or App) notes

##  DC_Large
Notes from Andrew Shumway:
https://lucidworks.atlassian.net/wiki/spaces/~621306800/pages/1947369512/Using+the+standard+DC+Large+APP+to+kickstart+a+project


## Objects.json transformation specific to dployeing DC_Large as Acme_DigiCommerce app

Replace all occurances of a string with another string.
e.g. replace "DC_Large" with a client-specific phrase like "Acme_DigiCommerce"

Groovy's [JsonSlurper](http://docs.groovy-lang.org/next/html/gapi/groovy/json/JsonSlurper.html) will read/slurp the objects.json content into a Map/Collection combination representing the Fusion objects defined. 

JsonObjectTransformer will perform 'savvy' transformations of that objects.json based on provided `Rules`. These rules are currently simple Maps of rule-types: `copy`, `set`, and `remove`. 
Each rule type can have a list of specific rules. The individual rules are Maps of actions.

For example, [JsonObjectTransformeDeployTest.groovy](http://docs.groovy-lang.org/next/html/gapi/groovy/json/JsonSlurper.html) defines a single copy rule that copies (with transformation) all objects.json entries that have a value containing `DC_Large`. 
The current (July 2022)  representation of that ruleset is: 
```
[copy: [
        [sourcePath           : '.*',
         sourceItemPattern    : '~DC_Large',
         destinationExpression: 'Acme_DigiCommerce'],
],]
```
That is one copy-rule (a single value in the copy rules list)
The rule is a (Java) map, with three entries. 
`sourcePath` is the pattern defining what items to match in the parsed objects.json. In this case `.*` is the regex pattern to match everything/anything.

`sourceItemPattern` is another pattern to match on values of the items selected from the sourcePath matching _(e.g. `.*` for all items)_.

The tilde `~` is a custom syntax indicating we want to match anything in the source object with values **containing** the given value. So match all items that have `DC_Large` in the string values.
`destinationExpression` is optional, but when provided with `sourceItemPattern` that starts with a `~` it will replace all occurences of the sourceItemPattern with the destinationExpression. 

This has been tested with case sensitive replacements. The java/groovy command does use a regex replacement (https://docs.groovy-lang.org/latest/html/api/org/codehaus/groovy/runtime/StringGroovyMethods.html) so more advanced patterns may be available. 

The Spock unit test  [JsonObjectTransformeDeployTest.groovy](https://github.com/seanoc5/upval/blob/master/src/test/groovy/com/lucidworks/ps/transform/JsonObjectTransformeDeployTest.groovy) performs the search/replace and can be edited to add more in-depth testing of results and expectations. 

Basic overview of flow: 
* loads  the exported DC_Large.zip from the test resources folder,
* extracts the objects.json file from the zip
* slurps the json into a Java object (Map/Collections) for transforming
* walks the entire object and creates a "flat-map" of all items in the json
* reads a rule-set manually defined in the unit test
* duplicates (shallow clone) the source map to the destination map
* selects all source items (`.*` source path filter)
  * filters those to all items matching `DC_Large` in the string values 
  * replaces `DC_Large` with the value `Acme_DigiCommerce`


## TODO
The following things have not yet been implemented, but are on the todo-list:

### Set rules
This should be a minor extension of the copy rule logic/processing. Likely fairly straighforward to implement. Most of the effort will be defining sufficient test cases...?

### Remove rules
This should be a minor extension of the copy rule logic/processing. Likely fairly straighforward to implement. Most of the effort will be defining sufficient test cases...?


### XML processing
Need to port same logic/processing to XMLObjects.
This is fundamentally different, but possibly easier as XPath and XML tools are more aligned with transformations

### Blobs processing
Unknown transformations for DC_Large at the moment. Assume there are minor blob changes? 

### Configsets
Unknown transformations for DC_Large at the moment. Assume there are minor blob changes?


### Notes from Andrew:
    remove /^lw_.*/ pipelines
    remove /.*debug.*/  objects
    remove /^pref.*/  blobs
    transform JS (and other multi-line script like tags)
    remove top-level lastUpdated  (modified?) tags
    remove lop-level  updates tags
