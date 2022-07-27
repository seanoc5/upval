package configurations

rules.copy = [
        [sourcePath           : ~'.*',
         sourceItemPattern    : 'DC_Large',
         destinationExpression: 'Acme_DigiCommerce'
        ]
]

rules.rename = [
        [namePattern: 'DC_Large', replacement: 'AcmeDigicommerce'],
        [namePattern: ~/TYPEAHEAD/, replacement: 'acme_ta'],
        [namePattern: 'RESPONSE_TA', replacement: 'response_ta'],
        [namePattern: 'CAT_ID_MAPPING', replacement: 'category_mapping'],
]

rules.skip = ['_lw_tmp',
              'blobs/prefs'
]
