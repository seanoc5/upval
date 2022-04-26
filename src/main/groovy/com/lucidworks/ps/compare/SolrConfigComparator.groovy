package com.lucidworks.ps.compare

class SolrConfigComparator {

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
