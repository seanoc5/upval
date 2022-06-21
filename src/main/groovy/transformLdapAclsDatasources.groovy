import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing
import com.lucidworks.ps.transform.ObjectTransformerJayway
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.log4j.Logger

/**
 * early-going code exploring how to map-transform a 'thing',
 * chose ldap-acls because it has significant (but fairly easy) structural changes from f4->f5
 */
Logger log = Logger.getLogger(this.class.name);

log.info "start script ${this.class.name}..."

File objectsJsonFile = new File("/home/sean/work/lucidworks/Intel/exports/objects.F4.json")
log.info "parsing file: ${objectsJsonFile.absolutePath}"

JsonSlurper jsonSlurper = new JsonSlurper()

if (objectsJsonFile.exists()) {
    def rulesStream = getClass().getClassLoader().getResourceAsStream("mapping/F4-F5.ldap-acls.jayway.json");
    def rulesMap = jsonSlurper.parse(rulesStream)

    Map f4ParsedMap = ExtractFusionObjectsForIndexing.readObjectsJson(objectsJsonFile)

    // process actual objects
    Map f4Objects = f4ParsedMap.objects
    log.info "\t\tF4 Objects count: ${f4Objects.size()} \n\t${f4Objects.collect { "${it.key}(${it.value.size()})" }.join('\n\t')}"
    List<Map<String, Object>> datasSources = f4Objects.dataSources
    def f4LdapAclsDataSources = datasSources.findAll { it.type == 'ldap-acls' }

    File f5LdapAclsTemplateFile = new File('/home/sean/work/lucidworks/Intel/exports/F5.ldap-acls.testAcl.json')
    File outFolder = new File('/home/sean/work/lucidworks/Intel/exports/ldap')

    f4LdapAclsDataSources.each { Map f4SrcLdapMap ->
        Map f5LdapAclsTemplateMap = jsonSlurper.parse(f5LdapAclsTemplateFile)

        ObjectTransformerJayway transformer = new ObjectTransformerJayway(f4SrcLdapMap, f5LdapAclsTemplateMap, rulesMap)
        def f5TransformedMap = transformer.transform()
        JsonBuilder jsonBuilder = new JsonBuilder(f5TransformedMap)

        String f5Json = jsonBuilder.toPrettyString()
        File outFile = new File(outFolder, f4SrcLdapMap.id + ".json")
        outFile.text = f5Json
        log.info "\t\twrote file: ${outFile.absolutePath}"
    }

}
