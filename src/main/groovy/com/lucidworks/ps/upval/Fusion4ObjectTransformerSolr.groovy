package com.lucidworks.ps.upval

import org.apache.log4j.Logger
import org.apache.solr.common.SolrInputDocument

/**
 * Class to evaluate various Fusion 4 objects from objects.json (Fusion F4 app export)
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
 */
public class Fusion4ObjectTransformerSolr extends ExtractFusionObjectsForIndexing {
    //todo rethink this approach... if base class creates map which can be flattened to appropriate structure for json builder, do we really need this...? perhaps just a function to convert json to solrinput doc???
    static Logger log = Logger.getLogger(this.class.name);

    static List<SolrInputDocument> collections(List collections) {
        log.debug "Collection count: ${collections.size()} -- ${collections.collect { it.id }.join("\n\t")}"
        List<SolrInputDocument> sidList = []
        collections.each { Map map ->
            SolrInputDocument sid = new SolrInputDocument()
            map.each { String name, def val ->
                sid.addField(name, val)
            }
            sidList << sid
        }
        return sidList
    }

    static List<SolrInputDocument> indexPipelines(List indexPipelines) {
        log.debug "Collection: ${indexPipelines.size()} -- ${indexPipelines.collect { it.id }.join("\n\t")}"
        List<SolrInputDocument> sidList = []
        indexPipelines.each { Map map ->
            SolrInputDocument sid = new SolrInputDocument()
            map.each { String name, def val ->
                sid.addField(name, val)
            }
            sidList << sid
        }
        return sidList
    }

    static def queryPipelines(List queryPipelines) {
        log.info "Query Pipelines: ${queryPipelines.size()} -- ${queryPipelines.collect { it.id }.join("\n\t")}"
        List<SolrInputDocument> sidList = []
        queryPipelines.each { Map map ->
            SolrInputDocument sid = new SolrInputDocument()
            map.each { String name, def val ->
                sid.addField(name, val)
            }
            sidList << sid
        }
        return sidList
    }

    static def features(Map features) {
        log.info "features: ${features.size()} -- ${features.keySet().join('\n\t')}"
        List<SolrInputDocument> sidList = []
        features.each { def map ->
            SolrInputDocument sid = new SolrInputDocument()
            try {
                map.each { String name, List<Map> val ->
//                map.each {
                    log.debug "val: ${it.value}"
                    sid.addField(it.key, it.value)
                }
            } catch (Exception e){
                log.warn "Collection issue... ${e.message}"
            }
            sidList << sid
        }
        return sidList

    }

    static def indexProfiles(List indexProfiles) {
        log.info "indexProfiles: ${indexProfiles.size()} -- ${indexProfiles.collect { it.id }.join("\n\t")}"
    }

    static def queryProfiles(List queryProfiles) {
        log.info "queryProfiles: ${queryProfiles.size()} -- ${queryProfiles.collect { it.id }.join("\n\t")}"
    }

    static def parsers(List parsers) {
        log.info "parsers: ${parsers.size()} -- ${parsers.collect { it.id }.join("\n\t")}"
    }

    static def objectGroups(List objectGroups) {
        log.info "objectGroups: ${objectGroups.size()} -- ${objectGroups.collect { it.name }}"
    }

    static def links(List links) {
        def linksMap = links.groupBy { it.linkType }
        log.info "links: ${links.size()} -- ${linksMap.collect { it.key + ":" + it.value.size() }.join('\n\t')}"
    }

    static def tasks(List tasks) {
        Map tasksGrouped = tasks.groupBy { it.type }
        log.info "tasks: ${tasks.size()} -- ${tasks.collect { it.id }.join('\n\t')}"
//        log.info "tasks groups: ${tasksGrouped.size()} -- ${tasksGrouped.collect{it.key + ":"}}"
    }

    static def jobs(List jobs) {
        log.info "jobs: ${jobs.size()} -- ${jobs.collect { it.resource }.join('\n\t')}"
    }

    static def sparkJobs(List sparkJobs) {
        log.info "sparkJobs: ${sparkJobs.size()} -- ${sparkJobs.collect { it.id }.join('\n\t')}"
        Map sparkJobsGrouped = sparkJobs.groupBy { it.type }
        sparkJobsGrouped.each { String type, List jobs ->
            log.info "\t\t$type (${jobs.size()}) :: ${jobs.collect { it.id }.join(', ')}"
        }
    }

    static def blobs(List blobs) {
        log.info "blobs: ${blobs.size()} -- ${blobs.collect { it.id }.join('\n\t')}"
        Map blobsGrouped = blobs.groupBy { it.dir }
        log.info "Blobs grouped by dir: ${blobsGrouped.collect { "\"${it.key}\":${it.value.size()}" }.join('\n\t')}"
    }

    static def experiments(List experiments) {
        log.info "experiments: ${experiments.size()} -- ${experiments.collect { it.id }}"
    }

    static def fusionApps(List fusionApps) {
        log.info "fusionApps: ${fusionApps.size()} -- ${fusionApps.collect { it.id }}"

        fusionApps.each { Map app ->
            log.info "\t\t[${app.id}] ${app.name} -- \"${app.description}\""
        }
    }

    static def dataSources(List dataSources) {
        log.info "dataSources: ${dataSources.size()} -- ${dataSources.collect { it.id }}"
        Map datasourcesGrouped = dataSources.groupBy { it.type }
        datasourcesGrouped.each { String type, List dsList ->
            log.info "\t\t$type: ${dsList.collect { "${it.id}" }}"
        }
    }


}
