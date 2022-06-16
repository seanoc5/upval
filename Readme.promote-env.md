# Notes on Promoting environments

##  Fusion 5
### Preparation
- get `source`
  - App export (from F4+)
  - objects.json (F3...?)
  - Connection to `live` Fusion (4+) to read from
- Handle passwords
  - only datasources? DS exports will have `${secret.dataSources.my-ds.f.confluencePassword}` type placeholders
  - see buildPasswordFile.groovy for an example of how to get the list of possible passwords to merge/adapt/promote from one env to another
    - **todo**: _need to do the actual merging/promoting/mapping code... somewhere..._

### Promote
  - run the `migrateFusionApp.groovy` script (see command line args -help for details)
    - todo: _create a more focused 'promoteFusionApp.groovy' (currently the migrate script is more promote-than-migrate (mapping transforms not complete enough for challenging migrations)_



### Fusion 4
- Same as Fusion 5 for now
- If there are 'real' occurences, send tips, fixes, feedback (https://github.com/seanoc5/upval/)


### Process
Currently there is a groovy script `migrateFusionApp.groovy` that is a **recipe** for migrating an app. This can be improved, and is currently something like 85% complete.


