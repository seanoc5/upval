import com.lucidworks.ps.upval.ExtractFusionObjectsForIndexing

//import com.lucidworks.ps.upval.Fusion4ObjectTransformer

import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name);

log.info "start script ${this.class.name}..."
File objectsJsonFile = new File("./data/replaceme/objects.json")
log.info "parsing file: ${objectsJsonFile.absolutePath}"
if (objectsJsonFile.exists()) {
    Map parsedMap = ExtractFusionObjectsForIndexing.readObjectsJson(objectsJsonFile)

    // process actual objects
    Map objects = parsedMap.objects
    log.info "\t\tObjects count: ${objects.size()} \n\t${objects.collect { "${it.key}(${it.value.size()})" }.join('\n\t')}"
    List<Map<String,Object>> datasSources = objects.dataSources
    def fsDataSources = datasSources.findAll {it.type=='lucidworks.fs'}
    //Map<String, String> appInfo = Fusion4ObjectTransformer.getApplicationInfo(parsedMap, objectsJsonFile, sourceOwner)
    String names = fsDataSources.collect {
        it.id
    }
    log.info "Filesystem ds count: ${fsDataSources.size()}: ${names}"
}

log.info "done...?"
