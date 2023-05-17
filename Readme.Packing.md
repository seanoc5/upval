# Component Packaging 

## Overview
Packaging is the process of gathering Fusion elements (pipelines, datasources, configsets,...) and bundling them into a single component that can be deployed to a running destination Fusion. This assumes the component will be an importable zip file with an associated variables file.

## Processes/steps involved in packaging a component
- Select various elements from:
  - running fusion (tested with F5, possibly can work with earlier versions)
  - Exported App zip file
  - File system (local fs)
  - Source Control (github urls to raw code/files)
- Transform values
  - leaf-node values 
    - (i.e. start links in a datasource, or field names in a pipeline)
  - Structural paths in component
    - Configset or Blob "paths"
      - typically a folder structure in the zip file or exploded zip on the filesystem
      - e.g. myF5Export.zip -> /configsets/${foundry.destination.APP}_${foundry.FEATURE_NAME}
        - need to transform /configsets/Components/* to /configsets/${foundry.destination.APP}_${foundry.FEATURE_NAME} and add both of those variables to variables json file to include on import (deploy)

