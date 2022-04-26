# Overview of Upgrade Evaluator (UpVal) tool
This tool is meant to inspect an existing Fusion application (pre-F5), and help the migration to Fusion 5.

*Note:* sdkman (https://sdkman.io/install) is a helpful tool to install the JVM, Gradle, Groovy, and many other JVM-based tools. It also enables switching versions of those tools rather easily. 

## Pre-requisites
### Gradle
Gradle (https://gradle.org/) is a build tool which tends to be more flexible and powerful than Maven. This project defaults to Maven repositories and basic (gradle) build operations. 

### Groovy
Groovy (https://groovy-lang.org/) is a JVM based language that compiles back to straight java. 

### Fusion-j project
UpVal (this project) relies on Fusion-j for session based (basic auth) communication and functionality with Fusion. During this development phase, please clone the `Fusion-j` (https://github.com/seanoc5/fusion-j) repo as a 'sister' directory to `UpVal`.
The build.gradle in UpVal declares the dependency near the bottom of the file:
```    implementation project(':fusion-j')```
This allows seemless development (and build/compile) of both code bases simultaneously.

## Fusion 4 application migration
The script `migratefusionApp.groovy` provides command-line parsing and migration of an exported Fusion 4 application. The script will accept either an `objects.json` file, or the full exported zip file. In addition, the script requires a destination Fusion deployment (fusion url, username, and password).
Run the script without any params (or with the -h param) for details on the available (and required) parameters.


## Transformations

### General possibilities
#### StringTemplates
//            String stringFromDatabase = 'Hello ${name}!'
//            String name = 'world'
//            def engine = new groovy.text.SimpleTemplateEngine()
//            assert 'Hello world!'== engine.createTemplate(stringFromDatabase).make([name:name])
//            def bar = engine.createTemplate(value).make([

- http://www.groovyconsole.appspot.com/edit/22004?execute
- https://stackoverflow.com/questions/1472453/groovy-variable-substitution-in-dynamic-strings
- https://docs.groovy-lang.org/docs/next/html/documentation/template-engines.html
- https://stackoverflow.com/questions/37379101/how-to-convert-string-to-gstring-and-replace-placeholder-in-groovy
- https://docs.groovy-lang.org/latest/html/api/groovy/lang/GString.html
- https://blog.mrhaki.com/2009/08/groovy-goodness-string-strings-strings.html
- https://blog.mrhaki.com/2015/09/groovy-goodness-inspect-method-returns.html
- https://docs.groovy-lang.org/latest/html/api/groovy/util/Eval.html

### Upgrades & Migrations
- convert old format to new (minor syntax changes)
- reform old format to fundamentally different new format (i.e. Sharepoint -> sharepoint-optimized, field mapping)
- replay old implementation with recognized new best-practice impementation 

### Environment promotions

## List of groups from objects.json

### Applications


### collections [{...{solrParams}}]

Note: check any/all collections to ensure replicationFactor > 1

``` "id" : "active_products",
      "createdAt" : "2020-01-08T04:40:29.011Z",
      "searchClusterId" : "default",
      "commitWithin" : 10000,
      "solrParams" : {
        "name" : "active_products",
        "numShards" : 2,
        "replicationFactor" : 0,
        "pullReplicas" : 1,
        "maxShardsPerNode" : 4,
        "tlogReplicas" : 1
      },
```

#### Todo: add logic to handle autocreated collections that might not migrate well, or need to be migrated...?

### indexPipelines 
- [id,{stages:{id,type...condition,skip,,{Script:'''}[}}

### queryPipelines
- [id,{stages:{id,type...condition,skip,,{Script:'''}[}}
  - Advanced
    - type: text-tagger 
  - Basic
    - search-fields
    
### features  (enabeld==true)
- searchlogs
- signals
- recommendations
- partitionByTime
- ner
- 

### indexProfiles 
(easy)

### queryProfiles 
(easy)

### parsers
(medium?)
- id
- type
- enabled
- ...

tika differences?
html processing??
json: paths?
archive: conditions

### objectGroups
.... que?!? ...

### links
(lots of items, basic logic/processing??)

### tasks
- rest-call
  - uri
  - method
  - queryParams
  - headers
  - entity

### jobs
- resource
- enabled
- triggers
  - type
  - enabled
  - interval
  - timeUnit
  - startTimestamp

### sparkJobs
- id
- type
- experimentId
- metricName
- sql
- experiment
  - -id
  - uniqueIdParameter
  - baseSignalsCollection
  - variants [{}]
  - automaticallyAdjustTraffic
  - enabled
  - startTimestamp
  - runId
  - metrics [{}]

### blobs
- id
- path
- dir
- filename
- contentType
- size
- modifiedTime
- version
- md5
- metadata {resourceType:file}


### experiments
- id
- uniqueIdParameter
- baseSignalsCollection
- variants [{}]

### fusionApps
- id
- name
- description
- dataUri
- properties {}

### dataSources
- id
- created
- modified
- connector
- type
- pipeline
- properties 
  - initial_mapping
    - mappings [{}]

### Connectors that need translation
- ldap-acls
- sharepoint-optimized
  - v1: multiple start links (simulating sites)
  - v2: one start link, multiple site entries
  - need to do the conversion (check that all v1 startlinks are the same base, or create multiple v2 datasources for each base)
- type <~> connector 
  - type is used in ?export objects.json?
  - connector is in the API output
- lucidworks vs lucid prefix

### metadata


### Configsets (solr)
- managed schema
- solr config
- other additions/changes (xslt files?)

## Data management & transfer
- Signals
  - raw
  - aggregated
- Predictive merchandizer (?)
- 

## TODO -- next steps
- separate parsing from persisting
- look at options (extendings) for outputting to filesystem, solr, fusion/solr, source repo



## add connectors if missing
- get list of used (connector) plugins
- get list of installed plugins
- get list of repository plugins
- build list of 'missing' plugins
