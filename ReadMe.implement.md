# Implement components
**Note:** this is a script-driven implementation. This is different from **Deploy** in that this is focused on making the necessary calls to create a component on a destination Fusion.

**Deploy** is current (soon to be renamed?) the process of transforming content from the Foundry to then be uploaded into a destination Fusion.

## TypeAhead

### Pre-requisites: 
- Access to running (destination) Fusion
- know the zkhost string?? (probably not: should be able to "grab" it from the fusion api
- Values for (select) destination objects:
  - Typeahead name: typeahead
  - Source Catalog collection (optional, but typical)
    - collection name ("main" collection with titles, brands,...)
    - fields to pull for (entity) suggestions: e.g. title, brand, author,...
    
### Things to do:
- Blobs
  - lib/index/FusionServiceLib.js
  - TYPEAHEAD_DW/full-list-of-bad-words_csv-file_2018_07_30.csv
  - TYPEAHEAD_DW/Typeahead_inclusion_list.csv
- index pipeline
- index profile
- datasources
  - signals collection for query suggestions
  - catalog collection for catalog/entity suggestions
  - explicit values (file upload of predefined suggestions)
- query pipeline
- query profile
- task
- spark
- spark
- 
