package com.lucidworks.ps.solr

import com.lucidworks.ps.fusion.Application
import org.apache.log4j.Logger

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * wrapper class to help with solr schema parsing and operations
 */
class ConfigSetCollection {
    Logger log = Logger.getLogger(this.class.name);
    String appName          // can be empty if getting from solr direcltly (i.e. all apps all configsets...?)
    List<ConfigSet> configSets = []


    /**
     * Constructor with F4+ app export (zip) file to get
     * @param appExport
     */
    ConfigSetCollection(File appExport) {
        Application app = new Application(appExport)
        configSets = parseConfigsetCollection(app.configsetMap)
        log.info "Fusion app export file (${appExport.absoluteFile}) with parsed app:($app) constructor, with (${configSets.size()} config sets)..."
    }

    ConfigSetCollection(Application app) {
        configSets = parseConfigsetCollection(app.configsetMap)
        log.info "Fusion parsed app ($app) constructor, with (${configSets.size()} config sets)..."
    }

    ConfigSetCollection(Map configsetCollection) {
        configSets = parseConfigsetCollection(configsetCollection)
        log.info "Fusion configsetCollection (${configsetCollection.size()}) constructor, with (${configSets?.size()} config sets)..."
    }


    @Override
    public String toString() {
        def mainCollections = configSets.findAll { ConfigSet configSet ->
            configSet       // todo -- find everything that is not signals or support collection
        }

        return "ConfigSetCollection{" +
                "appName='" + appName + '\'' +
                ", configSetList=" + configSets +
                '}';
    }

//    List<ConfigSet> parseConfigsetCollection(LinkedHashMap<String, Object> configsetEntries, Pattern keyPattern= ~/.*configsets(\/[^\/]+\/).*/) {
    List<ConfigSet> parseConfigsetCollection(LinkedHashMap<String, Object> configsetEntries, Pattern keyPattern= ~/.*configsets\/([^\/]+)\/.*/) {
        log.info "parseConfigsetCollection with ${configsetEntries.size()} entries"
        def configsetsGrouped = configsetEntries.groupBy {String configPath, def val ->
            Matcher match = (configPath =~ keyPattern)
            String configName = 'n.a.'
            if(match.matches()) {
                configName = match[0][1]
                log.debug "Match: $configName"
            } else {
                log.debug "No match, leave as 'none available': $configPath"
            }
            return configName
        }
        configsetsGrouped.each { String configName, def items ->
            ConfigSet configSet = new ConfigSet(configName, items)
            configSets << configSet
        }

        return configsetsGrouped

    }
}
