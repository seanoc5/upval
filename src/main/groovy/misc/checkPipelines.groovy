package misc

//import com.lucidworks.ps.fusion.Application
import com.lucidworks.ps.js.PipelineJsAnalyzer
import com.lucidworks.ps.model.fusion.Application
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

Logger log = Logger.getLogger(this.class.name)

OptionAccessor options = ExportedAppArgParser.parse(this.class.name, args)

log.info "start script ${this.class.name}..."
//FusionClient fusionClient = new FusionClient(options)
File srcFile = new File(options.source)
if(srcFile && srcFile.canRead()){
    log.info "Source file: ${srcFile.absoluteFile}"
}

Application application = new Application(srcFile)
Map fusionParsedMap = application.exportedObjectsSourceMap

//todo add Application method: getJavascriptThings()?
Map<String, Object> objects = fusionParsedMap.objects
List<Map<String, Object>> foo = objects.indexPipelines
def indexPipelines = application.indexPipelines

def stages = indexPipelines.collect { it.stages }.flatten()
def jsStages = PipelineJsAnalyzer.collectJsStages(stages)

// bulk list (all index & query JS stage) of javascript code lines, will be filtered and analyzed at the end
List<String> jsCodeLines = []


def indexStageTypes = stages.groupBy { it.type }
log.info "Index pipeline stage types:\n" + indexStageTypes.collect { String key, def val -> "${val.size()} :: $key" }.join('\n')
File indexOutFile = new File("index.javascript.js")
indexOutFile.text = ''      // clear contents (if any)
def jsIndexTypes = ['javascript-index', 'managed-js-index']

jsIndexTypes.each { String stageType ->
    List<Map<String, Object>> checkStages = indexStageTypes[stageType]

    checkStages.each {
        String label = it.label ?: 'unknown label'
        String myscript = it.script

        if (myscript) {
            indexOutFile << "// ---------------- IDXP: $label --------------------\n"
            indexOutFile << myscript
            indexOutFile << '\n'

            jsCodeLines << "// ---------------- IDXP: $label --------------------\n"
            jsCodeLines.addAll(myscript.split('\n'))
            jsCodeLines << "\n\n"

        } else {
            log.warn "No script in Index stage?? $it"
        }
    }
}


List querystages = fusionParsedMap.queryPipelines.collect { it.stages }.flatten()
