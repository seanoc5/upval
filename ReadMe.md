# Overview of Upgrade Evaluator (UpVal) tool
This tool is meant to inspect an existing Fusion application (pre-F5), and help the migration to Fusion 5.

## Part 1: Migration LoE insight estimate
Overview:

Process `objects.json` and explode the various groups of objects (collections, pipelines, jobs, parsers,...) into sub-objects. Many of those  sub-objects will be expanded further into sub-subobjects as appropriate. The goal is to divide the large & complex objects.json into something of an atomic view where individual components can be evaluated. This helps looks for patterns as well as simplify the scope of various estimations. 

## Part 2: Migration assistance and (semi) automation

### Overview:

Once the objects.json "big picture" has been divided into atomic-ish components, we look to automating the migration where possible. Some components will migrate without changes, others may take considerable manual effort. We start with automating the easy parts. Over time, this process should gather best-practices and guidance on how to approach the more complex components

## Assumptions & Requirements

Destination Fusion collection expected to have `upval-json` index profile _(convenience profile to throw json definitions at)_

## Transformations
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


## TODO -- next steps
- separate parsing from persisting
- look at options (extendings) for outputting to filesystem, solr, fusion/solr, source repo



## add connectors if missing
- get list of used (connector) plugins
- get list of installed plugins
- get list of repository plugins
- build list of 'missing' plugins
