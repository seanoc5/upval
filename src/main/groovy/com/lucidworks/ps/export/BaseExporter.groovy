package com.lucidworks.ps.export

/**
 * class to help exporting in general
 * see extending classes for spcifics on Filesystem and others
 * todo implement: Fusion data store (github alternative), Fusion-j (send to live fusion),,...
 */
class BaseExporter {
    Map exportMappingRules = [ignore:[], transform:[], copy:[]]

    /**
     * main constructor - get a list of rules to help with "exporting"
     * @see com.lucidworks.ps.transform.ObjectTransformerJayway for similar (same?) functionality
     * @param exportMappingRules
     */
    BaseExporter(def thingToExport, Map exportMappingRules) {
        this.exportMappingRules = exportMappingRules
    }
}
