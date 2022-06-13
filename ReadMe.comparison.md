# Comparison strategy/operations overview

## Where to start
### Unit tests
- BaseComparatorTest.groovy
  - generic object comparison 
  - non-fusion, just comparing two hybrid maps (mixing map, list, and base objects such as strings and numbers)
- DataSourceCompareTest.groovy

## flattened path comparisons
"Flatten" the collection of objects (a 'slurped' json or xml configuration file or api response). 
Do this for both 'left' and 'right' (source and destination) instances. 

Build flattened paths for the object and descendents.
Compare the list<String> of flattened paths and look for leftOnly and rightOnly 'things'
Collect all the lists that match between left and right, then look for differences in those "shared" objects (i.e. attributes/values)


## Object comparisons
For any given "object" compare values.
This assumes the above 'path' comparisons is complete, and now we are considering individual 'objects' that match at a given 'level'.

Look for differences in property/attribute lists.
Report leftOnly and rightOnly attributes (i.e. missing or mismatched attributes)

For all the 'shared' attributes: compare the values. Enable a way to ignore select value differences (e.g. createDate, startURls,..)


## Details
### App comparison (e.g. F4 -> F5 migration)
DEFAULTTHINGSTOCOMPARE = "configsets collections dataSources indexPipelines queryPipelines parsers blobs appkitApps features objectGroups links sparkJobs".split(' ')

#### things to compare
- collections 
  - just metadata: ~10 things to compare
  - value compare: 
    - id, searchClusterId, solrParams, commitWithin, type, metadata
  - value ignore:
    - createdAt, updates
- dataSources 
  - value compare
    - id, connector, type, pipeline, properties~, 
  - value ignore
    - createdAt, modified, description
- indexPipelines 
  - id, stages, properties
- queryPipelines 
  - id, stages, properties
- parsers 
  - id, parserStages, *
- blobs 
  - *
- appkitApps 
  - *
- features
  - *
- objectGroups 
- links 
  - *
- sparkJobs
  - *

- configsets (see solr config comparison below)

### Solr config comparison

#### schema (managed-schema)

#### solrconfig.xml

#### configsets
Configsets are exported with an app export (f4+). It appears that only the config sets for the relevant app collections are exported with the app 

*Todo -- (confirm?)*


- all config sets
- tree
  - managed-schema


### Environment promotion (e.g. Dev to Staging)

#### Change detection
- Application change
  - same list as App Change -- things to compare -- above 
