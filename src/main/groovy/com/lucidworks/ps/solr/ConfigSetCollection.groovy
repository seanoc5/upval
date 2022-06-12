package com.lucidworks.ps.solr

import com.lucidworks.ps.fusion.Application
import org.apache.log4j.Logger
/**
 * wrapper class to help with solr schema parsing and operations
 */
class ConfigSetCollection {
    Logger log = Logger.getLogger(this.class.name);
    String appName          // can be empty if getting from solr direcltly (i.e. all apps all configsets...?)
    List<ConfigSet> configSetList


    /**
     * Constructor with F4+ app export (zip) file to get
     * @param appExport
     */
    ConfigSetCollection(File appExport) {
        Application app = new Application(appExport)
        configSetList = app.configsets
        log.info "Fusion app export file (${appExport.absoluteFile}) with parsed app:($app) constructor, with (${configSetList.size()} config sets)..."
    }

    ConfigSetCollection(Application app) {
        configSetList = app.configsets
        log.info "Fusion parsed app ($app) constructor, with (${configSetList.size()} config sets)..."
    }


    @Override
    public String toString() {
        def mainCollections = configSetList.findAll {ConfigSet configSet ->
            configSet       // todo -- find everything that is not signals or support collection
        }

        return "ConfigSetCollection{" +
                "appName='" + appName + '\'' +
                ", configSetList=" + configSetList +
                '}';
    }
}
