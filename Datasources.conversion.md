# Datasources conversion notes

## Ldap-acls
### Simple translation
- type
  - ldap-acls => lucidworks.lucidworks.ldap

### Questions/unresolved
- F4 source values
  - "f.userPrincipal" : "srvcacct@abc",
  - "f.adNetbiosDomain" : "ABC",
  - "collection" : "mcCollection",
  - "f.ldap_search_base" : "DC=abc,DC=corp,DC=abc,DC=com",
  - "f.password" : "${secret.dataSources.my_abc_acl.f.password}",
  
- F5 
  - acls collections (datasource hierarchy??) -- is `acls` still valid?
  - AD Server Properties  (is non-AD valid??)
  - Document Save Options  (AD Netbios domain??)
  
# Filesystem
## S3 bucket


## Sharepoint conversion notes
### Mapping
#### Known
#### Unknown 
- properties
  - startLinks
  - f.app_auth_client_secret
  - bulkImportCsvFormat
  - f.sharepoint_online
  - f.app_auth_tenant
  - f.proxyHost
  - f.password
  - f.adPassword
  - f.user_agent
  - f.app_auth_client_id
  - f.app_auth_azure_login_endpoint

### Advanced
startlinks[0] becomes webApplicationUrl (base)
then the paths of the startlinks added to siteCollections

Probably need a distinct connector for each startlinks base (can we assume only one base/host per f4 SPoptimized connector???)
