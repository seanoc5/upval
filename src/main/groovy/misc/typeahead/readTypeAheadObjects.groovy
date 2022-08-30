package misc.typeahead

import com.lucidworks.ps.clients.DeployArgParser
import com.lucidworks.ps.clients.FusionClient
import com.lucidworks.ps.clients.FusionResponseWrapper
import groovy.cli.picocli.OptionAccessor
import groovy.json.JsonOutput
import org.apache.log4j.Logger

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   8/4/22, Thursday
 * @description: read the typeahead config file,get various fusion objects for packaging
 *
 */

final Logger log = Logger.getLogger(this.class.name)
log.info "Starting ${this.class.name} with args: ${args.findAll { !it.startsWith('-p') }}..."

// ------------------ Configuration -------------------
OptionAccessor options = DeployArgParser.parse(this.class.name, args)
File cfgFile = options.config
//File cfgFile = new File(options.config)
ConfigObject config = null
ConfigSlurper configSlurper = new ConfigSlurper()
configSlurper.setBinding([userName:options.user, password:options.password])
if (cfgFile.exists()) {
    log.info "Reading config file: ${cfgFile.absolutePath}"
    config = configSlurper.parse(cfgFile.toURI().toURL())
} else {
    String msg = "Can't find config file: ${cfgFile.absolutePath}!! throwing error..."
    log.warn msg
    throw new IllegalArgumentException(msg)
}


// ------------------ Fusion Client -------------------
FusionClient fusionClient = new FusionClient(options)
String appID = 'Components'         // note: app name == Component_Packages, but we need to call appid
//def qryPipelines = fusionClient.getQueryPipelines(appID)
String qrypName = 'Components_TYPEAHEAD_DW_QPL_v4'
FusionResponseWrapper responseWrapper = fusionClient.getQueryPipeline(appID, qrypName)
Map qryp = responseWrapper.parsedMap
log.info "Query Pipeline: $qryp"

Map pkgMap = [objects: config.objects, metadata: config.metadata, properties:config.properties]
String json = JsonOutput.prettyPrint(JsonOutput.toJson(config.objects))
log.info "Objects: \n$json"

/*
// sample of apache commons compress (not in build at the moment)
BufferedOutputStream bufferedOutputStream = null;
    ZipArchiveOutputStream zipArchiveOutputStream = null;
    OutputStream outputStream = null;
    try {
        Path zipFilePath = Paths.get(zipFileName);
        outputStream = Files.newOutputStream(zipFilePath);
        bufferedOutputStream = new BufferedOutputStream(outputStream);
        zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream);
        File fileToZip = new File(fileOrDirectoryToZip);

        addFileToZipStream(zipArchiveOutputStream, fileToZip, "");

        zipArchiveOutputStream.close();
        bufferedOutputStream.close();
        outputStream.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
*/
String outPath = options.exportDir
if (outPath) {
    log.info "Using exportDir: $outPath"
} else {
    log.warn "Could not find exportDir in options, defaulting to './'  "
    outPath = './'
}
File outDir = com.lucidworks.ps.Helper.getOrMakeDirectory(outPath)
File outFile = new File(outDir, "TA-sample.zip")


ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
ZipEntry e = new ZipEntry("objects.json");
out.putNextEntry(e);

byte[] data = json.getBytes();
out.write(data, 0, data.length);
//out.write();
out.closeEntry();

out.close();

log.info "wrote file: ${outFile.absolutePath}"

log.info "Done...?"
