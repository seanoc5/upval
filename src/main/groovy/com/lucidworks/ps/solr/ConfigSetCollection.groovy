package com.lucidworks.ps.solr


import org.apache.log4j.Logger

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * wrapper class to help with solr schema parsing and operations
 */
class ConfigSetCollection {
    Logger log = Logger.getLogger(this.class.name);
    String deploymentName          // can be empty if getting from solr direcltly (i.e. all apps all configsets...?)
    Map<String, List<ConfigSet>> configsetMap = [:]
    public static final Pattern DEFAULT_KEY_PATTERN = ~/.*configsets\/([^\/]+)(\/.*)/
    // first group is the configset name (collection-name), second group is the path to use in the configset


    /**
     * Constructor with F4+ app export (zip) file to get
     * @param appExport
     */
/*
    ConfigSetCollection(File appExport) {
        Application app = new Application(appExport)
        def rc = parseConfigsetCollection(app.configsetMap)
        log.info "Fusion app export file (${appExport.absoluteFile}) with parsed app:($app) constructor, with (${configsetMap.size()} config sets)..."
    }
*/

/*
    ConfigSetCollection(Application app) {
        configsetMap = parseConfigsetCollection(app.configsetMap)
        log.info "Fusion parsed app ($app) constructor, with (${configsetMap.size()} config sets)..."
    }
*/

    ConfigSetCollection(Map configsetCollection, String deploymentName) {
        Integer collectionSize = parseConfigsetCollection(configsetCollection, deploymentName)
        log.info "Fusion configsetCollection (${configsetCollection.size()}) constructor, with (${collectionSize} config sets)..."
    }


    @Override
    public String toString() {
        return "ConfigSetCollection: $deploymentName with (${configsetMap.size()}) collection configsets"
    }

    /**
     * split the flat=list entries into implicit collection groups, doing some extra parsing in the ConfigSet constructor
     * @param configsetEntries
     * @param keyPattern
     * @return
     */
    protected Integer parseConfigsetCollection(LinkedHashMap<String, Object> configsetEntries, String deploymentName = 'Unnamed', Pattern keyPattern = DEFAULT_KEY_PATTERN) {
        log.info "parseConfigsetCollection with ${configsetEntries.size()} entries"
        if (deploymentName) {
            log.debug "setting deploymentName: $deploymentName"
            this.deploymentName = deploymentName
        }
        Map<String, Map<String, Object>> groupedItems = [:].withDefault { [:] }
        configsetEntries.each { String configPath, def val ->
            if (val) {
                Matcher match = (configPath =~ keyPattern)
                String configName
                String childPath
                if (match.matches()) {

                    configName = match[0][1]
                    childPath = match[0][2]
//                Map newkey = [collection: configName, childPath: childPath]
                    groupedItems[configName][childPath] = val
                } else {
                    log.warn "Pattern does not match-- path:$configPath - val:$val"
                }
            } else {
                log.info "No value for item: $configPath -> $val -- skipping(...?)"
            }
        }

        def configsetsGrouped = configsetEntries.groupBy { String configPath, def val ->
            Matcher match = (configPath =~ keyPattern)
            String configName
            if (match.matches()) {
                configName = match[0][1]
                log.debug "Match: $configName"
            } else {
                log.debug "No match, leave as 'none available': $configPath"
                configName = ''
            }
            return configName
        }
        configsetsGrouped.each { String configName, def items ->
            if (configName) {
//                def itemsTrimmed = items.collectEntries {}
                ConfigSet configSet = new ConfigSet(configName, items)
                configsetMap[configName] = configSet
            } else {
                log.info "\t\tSkipping 'empty' configset path: $configName with items (empty??): $items"
            }
        }
        // is there a better return value? true/false?  void?
        return configsetMap.size()
    }
}
