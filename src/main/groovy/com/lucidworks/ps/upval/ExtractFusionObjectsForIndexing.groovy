package com.lucidworks.ps.upval

import groovy.json.JsonSlurper
import org.apache.log4j.Logger

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Class to transform various Fusion 4 objects from objects.json (Fusion F4 app export)
 *
 * Known object types:
 * collections,
 * indexPipelines, queryPipelines,
 * features,
 * indexProfiles, queryProfiles,
 * parsers,
 * objectGroups, links,
 * tasks, jobs, sparkJobs,
 * blobs, experiments,
 * fusionApps,
 * dataSources
 * @deprecated @see FusionClient for some more recent fusion-aware access
 */
public class ExtractFusionObjectsForIndexing {
    static Logger log = Logger.getLogger(this.class.name);
    public Map connectorPluginRemapping = [
            'lucid.sharepoint-optimized': 'lucidworks.sharepoint-optimized',
            'lucid.ldap-acls'           : 'lucidworks.ldap-acls',
    ]

    /**
     * Helper function to parse the file (application export, or just the objects.json)
     *
     * @param appOrJson
     * @return Java Map with all of the 'standard' objects defined
     */
    static Map readObjectsJson(File appOrJson) {
        Map parsedMap = null
        if(appOrJson?.exists()) {
            String jsonString = null
            if (appOrJson?.exists() && appOrJson.isFile()) {
                if (appOrJson.name.endsWith('.zip')) {
                    ZipFile zipFile = new ZipFile(appOrJson)
                    Enumeration<? extends ZipEntry> entries = zipFile.entries()
                    entries.each { ZipEntry zipEntry ->
                        if (zipEntry.name.contains('objects.json')) {
                            InputStream inputStream = zipFile.getInputStream(zipEntry)
                            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                            jsonString = br.text
                            log.debug "\t\textracted json text from zip entry: $jsonString"
                            parsedMap = new JsonSlurper().parseText(jsonString)

                        }
                        log.debug "ZipEntry: $zipEntry"
                    }
                } else if (appOrJson.name.endsWith('json')) {
                    jsonString = appOrJson.text
                    log.info "Get json from json file: $appOrJson -- length: ${jsonString.size()} characters"
                    parsedMap = new JsonSlurper().parseText(jsonString)

                } else {
                    log.warn "Unknow file for objects.json contents: $appOrJson (${appOrJson.absolutePath}"
                }
            } else {
                log.warn "File arg ($appOrJson) either does not exist, or is not a (readable) file. Nothing to read from. Cancelling..."
                throw new IllegalArgumentException("No valid source file: $appOrJson")

            }
        } else {
            String msg = "No valid source file: $appOrJson"
            log.warn msg
            throw new IllegalArgumentException(msg)
        }

        return parsedMap
    }

    /**
     * get basic metadata about this export/objects.json file
     */
    static def getApplicationInfo(Map<String, Object> exportedJson, String owner) {
        def objects = exportedJson.objects
        def metadata = exportedJson.metadata
        if (objects.fusionApps?.size() > 1) {
            log.warn "More than one fusion app in this objects export??!? Bailing by throwing an exception because I have not see this structure before..."
            Exception e = new IllegalArgumentException("Multiple Applications where we expected only one app for this objects collection. Add logic to parse multiple apps to this class (${this.class.name})")
        } else if (objects.fusionApps?.size() == 0) {
            log.warn "No fusion app in this objects export??!? Bailing by throwing an exception because I have not see this structure before..."
            Exception e = new IllegalArgumentException("No Applications found where we expected one app for this objects collection. Add logic to parse multiple apps to this class (${this.class.name})")
        }

        Map app = objects.fusionApps[0]
        Map info = [
                appName        : app.id,
                appType        : app.name,
                appGuid        : metadata.fusionGuid,
                appVersion     : metadata.fusionVersion,
                appExported_tdt: metadata.exportedDate,
        ]

        if (owner) {
            log.info "Adding 'owner' ($owner) to returned app info "
            info.owner = owner
        }

        log.info "App info: $info"
        return info
    }


    /**
     * add the app info to each thing to be indexed, and set a consistent id which should be more unique when adding many apps
     * @param objType
     * @param itemMap
     * @param appInfo
     * @return
     */
    static Map buildItemMap(String objType, Map srcMap, Map appInfo, Map parentMap = null) {
        String compositeId = null
        Map itemMap = new HashMap(srcMap)
        if (!itemMap) {
            log.warn "Could not clone srcMap: $srcMap... via constructor, trying to copy with .clone()..."
            itemMap = srcMap.clone()
        }
        if (itemMap) {
            itemMap << appInfo
            itemMap.objectType = objType

            if (itemMap.id != null) {           // obj types: collection,
                String id = itemMap.id
                if (!id.trim()) {
                    log.warn "Blank ID: $objType - $itemMap"
                } else {
                    itemMap.objectId = itemMap.id
                    itemMap.id = "${objType}:${itemMap.id}::${appInfo.appGuid}:::${appInfo.appExported_tdt}"
                    log.debug "t\tswapped obj id to 'objectId': ${itemMap.objectId}, and solr doc id is compositeId: $compositeId"
                }

            } else if (objType == 'features') {
                log.debug "handle inconsistent data structure: no id for $objType..."
                String id = "${itemMap.collectionId}-${itemMap.name}"
                compositeId = "${objType}:${id}::${appInfo.appGuid}:::${appInfo.appExported_tdt}"
                itemMap.objectId = itemMap.id
                itemMap.id = compositeId
                log.debug "t\tswapped obj id to 'objectId': ${itemMap.objectId}, and solr doc id is compositeId: $compositeId"


            } else if (objType == 'links') {
                log.debug "handle inconsistent data structure: no id for $objType..."
                String id = "${itemMap.object}-${itemMap.subject}"
                compositeId = "${objType}:${id}::${appInfo.appGuid}"
                itemMap.objectId = itemMap.id
                itemMap.id = compositeId
                log.debug "t\tswapped obj id to 'objectId': ${itemMap.objectId}, and solr doc id is compositeId: $compositeId"

            } else if (objType == 'jobs') {
                log.debug "handle inconsistent data structure: no id for $objType..."
                String id = "${itemMap.resource}"
                itemMap.objectId = id
                compositeId = "${objType}:${id}::${appInfo.appGuid}"
                itemMap.id = compositeId

            } else if (objType == 'searchCluster') {
                log.debug "handle inconsistent data structure: no id for $objType..."
                String id = "${objType}-${itemMap.id}"
                itemMap.objectId = id
                compositeId = "${objType}:${id}::${appInfo.appGuid}"
                itemMap.id = compositeId

            } else { //if(itemMap.resource){
                log.error "No object Id?? $itemMap -- throwing an error in panic..."
                throw new IllegalArgumentException("No object ID (type: $objType) in itemMap (${itemMap}) ")
            }

            if (parentMap) {
                if (parentMap.id) {
                    itemMap.parentId = parentMap.id
                } else {
                    log.warn "Got a parent map, but no id for parent... what do we use for parentId...? $parentMap"
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug "no parent map to assign parent id: $itemMap"
                }
            }
        } else {
            log.warn "Trouble copying/cloning srcMap: $srcMap"
        }

        return itemMap
    }


    /**
     * transform fusion collections map for better indexing and analysis
     * mostly unchanged for collections (nothing needed to flatten or isolate...)
     *
     * @param srcList
     * @return Map
     */
    static List<Map<String, Object>> collections(List srcList, Map<String, String> appInfo) {
        List<Map<String, Object>> things = []
        String objType = 'collection'
        if (srcList) {
            log.info "\t\t$objType count: ${srcList.size()} -- ${srcList.collect { it.id }}"
            srcList.each { Map itemMap ->
                Map m = buildItemMap(objType, itemMap, appInfo)
                if (!m.label) {
                    String label = "Coll:${m.objectId} [${m.type}]"
                    log.debug "Add label to colletion $label"
                    m.label = label
                }

                things << m
            }
        } else {
            log.warn "No ${objType} to process??!! $srcList"
        }
        return things
    }

    /**
     *
     * @param srcList
     * @param appInfo
     * @return
     */
    static List<Map<String, Object>> searchClusters(def srcList, Map<String, String> appInfo) {
        List<Map<String, Object>> things = []
        String objType = 'searchCluster'
        if (srcList) {
            log.info "\t\t$objType count: ${srcList.size()} -- ${srcList.collect { it.id }}"
            srcList.each { Map itemMap ->
                Map m = buildItemMap(objType, itemMap, appInfo)
                if (!m.label) {
                    String label = "SearchCluster:${m.id} [${m.type}]"
                    log.info "\t\tAdd label to colletion $label"
                    m.label = label
                }

                things << m
            }
        } else {
            log.warn "No ${objType} to process??!! $srcList"
        }
        return things
    }


    static List<Map<String, Object>> indexPipelines(List srcList, Map<String, String> appInfo) {
        String objType = 'indexPipeline'
        List<Map<String, Object>> things = []
        if (srcList) {
            log.info "\t\t$objType count: ${srcList.size()} -- ${srcList.collect { it.id }}"
            srcList.each { Map itemMap ->
                def aim = buildItemMap(objType, itemMap, appInfo)
                things << aim
                itemMap.stages.each { Map stage ->
                    Map aimStage = buildItemMap("${objType}-stage", stage, appInfo, itemMap)
                    things << aimStage
                }
            }
        } else {
            log.warn "No ${objType} to process??!! $srcList"
        }
        return things
    }

    static def queryPipelines(List srcList, Map<String, String> appInfo) {
        String objType = 'queryPipeline'
        log.info "\t\tQuery Pipelines: ${srcList.size()} -- ${srcList.collect { it.id }}"
        List<Map<String, Object>> things = []
        if (srcList) {
            log.info "\t\t$objType count: ${srcList.size()} -- ${srcList.collect { it.id }}"
            srcList.each { Map itemMap ->
                things << buildItemMap(objType, itemMap, appInfo)
                itemMap.stages.each { Map stage ->
                    things << buildItemMap("${objType}-stage", stage, appInfo, itemMap)
                }
            }
        } else {
            log.warn "No ${objType} to process??!! $srcList"
        }
        return things
    }

    static def features(Map features, Map<String, String> appInfo) {
        String objType = 'features'
        log.info "\t\t$features: ${features.size()} -- ${features.keySet()}"
        List<Map<String, Object>> things = []
        if (features) {
            features.each { String key, List<Map> values ->
                values.each { Map<String, Object> valMap ->
                    valMap.featureName = key
                    things << buildItemMap(objType, valMap, appInfo)
                }
            }
        } else {
            log.warn "No features found to process...?? ${features}"
        }
        return things
    }

    static def indexProfiles(List<Map<String, Object>> indexProfiles, Map<String, String> appInfo) {
        String objType = 'indexProfiles'
        List<Map<String, Object>> things = []
        if (indexProfiles) {
            log.info "\t\t$objType: ${indexProfiles.size()} -- ${indexProfiles.collect { it.id }}"
            things = indexProfiles.collect {
                buildItemMap(objType, it, appInfo)
            }
        } else {
            log.warn "No source $objType: $indexProfiles...??!"
        }
        return things
    }

    static def queryProfiles(List<Map<String, Object>> queryProfiles, Map<String, String> appInfo) {
        List<Map<String, Object>> things = []
        String objType = 'queryProfiles'
        if (queryProfiles) {
            log.info "\t\tqueryProfiles: ${queryProfiles.size()} -- ${queryProfiles.collect { it.id }}"
            things = queryProfiles.collect {
                buildItemMap(objType, it, appInfo)
            }
        } else {
            log.warn "No $objType collection, skipping...? $queryProfiles"
        }
        return things
    }

    static def parsers(List parsers, Map<String, String> appInfo) {
        List<Map<String, Object>> things = []
        String objType = 'parsers'
        if (parsers) {
            log.info "\t\tparsers: ${parsers.size()} -- ${parsers.collect { it.id }}"
            parsers.each { Map parserMap ->
                things << buildItemMap(objType, parserMap, appInfo)

                parserMap.parserStages.each { Map stageMap ->
                    things << buildItemMap("${objType}-stage", stageMap, appInfo, parserMap)
                }
            }
        } else {
            log.warn "No $objType collection to process: $parsers"
        }
        return things
    }

    static def objectGroups(List objectGroups, Map<String, String> appInfo) {
        List<Map<String, Object>> things = []
        String objType = 'objectGroups'
        if (objectGroups) {
            log.info "${objType}: ${objectGroups.size()} -- ${objectGroups.collect { it.name }}"
            things = objectGroups.collect {
                buildItemMap(objType, it, appInfo)
            }
        }
        return things
    }

    static def links(List links, Map<String, String> appInfo) {
        def linksMap = links.groupBy { it.linkType }
        log.info "\t\tlinks: ${links.size()} -- ${linksMap.collect { it.key + ":" + it.value.size() }}"
        List<Map<String, Object>> things = []
        String objType = 'links'
        if (links) {
            things = links.collect {
                buildItemMap(objType, it, appInfo)
            }
        }
        return things
    }

    static def tasks(List tasks, Map<String, String> appInfo) {
        Map tasksGrouped = tasks.groupBy { it.type }
        log.info "\t\ttasks: ${tasks.size()} -- ${tasks.collect { it.id }}"
        List<Map<String, Object>> things = []
        String objType = 'tasks'
        if (tasks) {
            things = tasks.collect {
                buildItemMap(objType, it, appInfo)
            }
        } else {
            log.warn "no $objType collection to process...? $tasks "
        }
        return things
    }

    static def jobs(List jobs, Map<String, String> appInfo) {
        log.info "\t\tjobs: ${jobs.size()} -- ${jobs.collect { it.resource }}"
        List<Map<String, Object>> things = []
        String objType = 'jobs'
        if (jobs) {
            things = jobs.collect {
                buildItemMap(objType, it, appInfo)
            }
        }
        return things

    }

    static def sparkJobs(List sparkJobs, Map<String, String> appInfo) {
        Map sparkJobsGrouped = sparkJobs.groupBy { it.type }
        sparkJobsGrouped.each { String type, List jobs ->
            log.info "\t\t$type (${jobs.size()}) :: ${jobs.collect { it.id }.join(', ')}"
        }
        List<Map<String, Object>> things = []
        String objType = 'sparkJobs'
        if (sparkJobs) {
            things = sparkJobs.collect {
                buildItemMap(objType, it, appInfo)
            }
        }
        return things
    }

    static def blobs(List blobs, Map<String, String> appInfo) {
        log.info "\t\tblobs: ${blobs.size()} -- ${blobs.collect { it.id }}"
        Map blobsGrouped = blobs.groupBy { it.dir }
        log.debug "Blobs grouped by dir: ${blobsGrouped.collect { "\"${it.key}\":${it.value.size()}" }.join('\t')}"
        List<Map<String, Object>> things = []
        String objType = 'blobs'
        if (blobs) {
            things = blobs.collect {
                buildItemMap(objType, it, appInfo)
            }
        }
        return things
    }

    static def experiments(List experiments, Map<String, String> appInfo) {
        String objType = 'experiments'
        log.info "${objType}: ${experiments.size()} -- ${experiments.collect { it.id }}"
        List<Map<String, Object>> things = []
        if (experiments) {
            things = experiments.collect {
                buildItemMap(objType, it, appInfo)
            }
        }
        return things
    }

    static def fusionApps(List fusionApps, Map<String, String> appInfo) {
        String objType = 'fusionApps'
        log.info "${objType}: ${fusionApps.size()} -- ${fusionApps.collect { it.id }}"
        List<Map<String, Object>> things = []
        if (fusionApps) {
            if (fusionApps.size() > 1) {
                log.warn "More than one fusion app in fusion app export...!!??! -- $fusionApps"
            }
            things = fusionApps.collect {
                buildItemMap(objType, it, appInfo)
            }
        } else {
            log.warn "No $objType collection to process??!? $fusionApps"
        }
        return things
    }

    static def dataSources(List dataSources, Map<String, String> appInfo) {
        String objType = 'dataSources'
        log.info "${objType}: ${dataSources.size()} -- ${dataSources.collect { it.id }}"
        Map datasourcesGrouped = dataSources.groupBy { it.type }
        datasourcesGrouped.each { String type, List dsList ->
            log.info "\t\t$type: ${dsList.collect { "${it.id}" }}"
        }
        List<Map<String, Object>> things = []
        if (dataSources) {
            things = dataSources.collect {
                buildItemMap(objType, it, appInfo)
            }
        }
        return things

    }

}
