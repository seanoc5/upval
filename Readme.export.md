# Exporting Fusion objects/app

## To exploded filesystem

Goals:
Export the significant `things` to a (local) filesystem for review and processing.

Caveats:
Choose to ignore some things:
From: 
```
If we abstract up one level from the “what to remove/keep to make diff work” question it’s easy to see that the function which changes the response data from Fusion before making it available for GitHub is a transformation i.e. center lane function.If transformations are configurable via a manifest or set of metadata saying which things to transform and how, Is it not possible to have a default set which specifies

    remove /^lw_.*/ pipelines
    remove /.*debug.*/  objects
    remove /^pref.*/  blobs
    transform JS (and other multi-line script like tags)
    remove top-level lastUpdated tags
    remove lop-level  updates tags

Put that as a default transformation template for any project and leave it at that.  If it’s wrong for a project, it can be adjusted.  If the adjustment is continually needed, it can be made part of the default.
```
