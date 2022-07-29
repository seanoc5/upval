package configurations

rules.copy = [
        [sourcePath           : ~'.*',
         sourceItemPattern    : 'DC_Large',
         destinationExpression: 'Acme_DigiCommerce'
        ]
]

// potentially keep blob and filesystem renames separate, but that may not be necessary. consider merging the rules below into the rules above.
rules.rename = [
        [namePattern: 'DC_Large', replacement: 'AcmeDigicommerce'],
        [namePattern: ~/TYPEAHEAD/, replacement: 'acme_ta'],
        [namePattern: 'RESPONSE_TA', replacement: 'response_ta'],
        [namePattern: 'CAT_ID_MAPPING', replacement: 'category_mapping'],
]

rules.remove = [
        [pathPattern: 'updates', valuePattern: ''],
//        [pathPattern: '_lw_tmp', valuePattern: ''],
//        [pathPattern: 'blobs/prefs', valuePattern: ''],
]
