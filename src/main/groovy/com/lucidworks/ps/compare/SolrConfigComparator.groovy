package com.lucidworks.ps.compare

import com.lucidworks.ps.upval.Helper
import org.apache.log4j.Logger

import java.util.regex.Pattern

/**
 * deprecated?? old approach -- remove me?
 */
class SolrConfigComparator {
    static final Logger log = Logger.getLogger(this.class.name);
    public static final Map<String, Pattern> ATTR_NAMES_DEFAULT = ['':~/(name|id|source|dest|type|class)/]
//    public static final Map<String, Pattern> ATTR_NAMES_SCHEMA =
//    public static final Map<String, Pattern>  =
//    public static final Map<String, Pattern>  =

    /**
     * Compare solr configs (only schema has been tested at the moment)
     *  todo -- add logic to compare values (not just paths), and ignore some value differences
     * @param left
     * @param right
     * @param attribsForPath    a map of node name and pattern to include identifying attributes in the 'path' for comparison, empty string is catch-all/default
     * @return
     */
    static def compareXmlObjects(Node left, Node right, Map<String, Pattern> attribsForPath = ATTR_NAMES_DEFAULT){
        List<Map<String, Object>> leftPaths = Helper.flattenXmlPathWithAttributes(left, 0, '/', attribsForPath)
        List<String> leftNames = leftPaths.collect{it.name}
        List<Map<String, Object>> rightPaths = Helper.flattenXmlPathWithAttributes(right, 0, '/', attribsForPath)
        List<String> rightNames = rightPaths.collect{it.name}

        def leftOnly = leftNames - rightNames
        def rightOnly = rightNames - leftNames
        def shared = leftPaths.intersect(rightPaths)
        log.info "Left only (${leftOnly.size()}): $leftOnly"
        log.info "Right only (${rightOnly.size()}): $rightOnly"
        log.debug "Shared: $shared"
        CompareCollectionResults comparisonResult = new CompareCollectionResults('Solr Schemas', left,right, [])
        comparisonResult.leftOnlyIds = leftOnly
        comparisonResult.rightOnlyIds = rightOnly
        comparisonResult.sharedIds = shared
        return comparisonResult
    }

    def findUnusedSchemaThings(Map<String, Object> schema, Map<String, Object> lukeResults){
        log.info "Analyze schema and luke (for actual fields used as a snapshot in time)..."
        def fields = schema.findAll {}
    }

    /**
     * @deprecated? first pass, consider removing...
     * @param templateSchema
     * @param deployedSchema
     * @return
     */
    static ComparisonResult compareSchemaUniqueIds(Node templateSchema, Node deployedSchema) {
        String label = 'Solr Unique IDs'
        ComparisonResult result = null

        NodeList keytempl = templateSchema.uniqueKey
        NodeList keyDep = deployedSchema.uniqueKey
        if (keytempl.size() == 1 && keyDep.size() == 1) {
            Node uniqTemplate = keytempl[0]
            String idTemplate = uniqTemplate.value()
            Node uniqDep = keyDep[0]
            String idDep = uniqDep.value()
            if (idTemplate == idDep) {
                result = new ComparisonResult(label, true, "Unique IDs match, template:$idTemplate  == Deployment: $idDep")
            } else {
                result = new ComparisonResult(label, false, "Unique IDs do NOT match, template:$idTemplate  == Deployment: $idDep")
            }
        } else {
            result = new ComparisonResult(label, false, "unique key sizes do not match, template(${keytempl.size()}) != deployment (${keyDep.size()})")
        }
        return result
    }
}
