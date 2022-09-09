package configs

import com.lucidworks.ps.clients.FusionClient

/**
 * Simple Configuration exploring how to list components and transformations for packaging
 *
 */

// -------------------  params to configure source (mainly fusion-j) for fusion to pull definitions or objects from: -------------------
source {
    url = "${fusionUrl ?: 'http://foundry.lucidworksproserve.com:6764'}"
    user = "${userName ?: 'admin'}"
    password = "${password ?: 'password123'}"
    application = "${appName ?: 'Components'}"
}


// ------------------- destination-specific settings -------------------
destination {
    version = "${version ?: '5.5.1'}"
    outputZipPath = "${exportDir: '.'}"            // todo -- decide on string or actual file...
}

// ------------------ Packaging information -------------------
pkg {
    name = 'TestPkg'
    version = '0.1'
    transform {
        appName {
            paths = [
                    '$.objects',
            ]

        }
    }
}

// ------------------- helper utils for config reading/processing -------------------
// exampLe of config-is-code: create a working helper class to get configs directly from a running fusion (i.e. foundry)
fusionClient = new FusionClient(source.url, source.user, source.password, source.application)


// --------------------- components/objects to grab, transform, and bundle: typically objects.json, configsets, and blobs  ---------------------
objectsJson {
    // awkward naming/structure: file is called `objects.json` and has 3 top-level trees called: objects, blobs, configsets
    // NB: objectsJson.objects.blobs is the definition of blob objects, the "actual" blobs are "files" under: objectsJson.blobs
    objects {
        // switch from 'typical' config syntax like "objects {..}" to setting
        // a variable/leaf-node with standard groovy assignment (necessary to set a list rather than map)
        queryPipelines = [
                fusionClient.getQueryPipeline('Components_TYPEAHEAD_DW_QPL_v4', source.application),
        ]
        indexPipelines = [
                fusionClient.getIndexPipeline('Components_TYPEAHEAD_DW_IPL_v4', source.application),
        ]
    }

    metadata {
        formatVersion = "1"
        exportedBy = "admin"
        exportedDate = new Date()
        fusionVersion = destination.version
        fusionGuid = ""
    }
}
