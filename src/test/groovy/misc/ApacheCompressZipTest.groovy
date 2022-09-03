package misc

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
/**
 * testing how to create zipfile with zip entries
 */
class ApacheCompressZipTest extends Specification {
//    @Rule
//    public TemporaryFolder tempDir = new TemporaryFolder()            // couldn't get this work, and File.createTempFile seems a bit 'lighter'...


    def "create java.util.zip file with string source entries"() {
        given:
        Map<String, String> thingsToSave = [
                "file1.txt": 'this is the first text file text to store',
                "file2.txt": 'second text file text to store here',
        ]
//        File outFile = tempDir.newFile("TA-sample.zip")
        File outFile = File.createTempFile("TA-java-util", ".zip")
        println("Temp outfile: ${outFile.absolutePath}")

        when:
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile))
        thingsToSave.each { name, body ->
            ZipEntry e = new ZipEntry(name)
            out.putNextEntry(e)

            byte[] data = body.getBytes()
            out.write(data, 0, data.length)
            out.closeEntry()
        }
        out.close()


        then:
        outFile.exists()
        outFile.size() > 0
    }

    def "create zip file with string source entries part 2"() {
        given:
        Map<String, String> thingsToSave = [
                file1: 'this is the first text file text to store',
                file2: 'second text file text to store here',
        ]
        File outFile = File.createTempFile("TA-apache", ".zip")
        println("Temp outfile: ${outFile.absolutePath}")

        when:
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(outFile)
        thingsToSave.each { name, txt ->
            ZipArchiveEntry entry = new ZipArchiveEntry(name)
            entry.setSize(txt.size())
            zaos.putArchiveEntry(entry)

            // https://simplesolution.dev/java-create-zip-file-using-apache-commons-compress/
            InputStream iois = IOUtils.toInputStream(txt, StandardCharsets.UTF_8);
            IOUtils.copy(iois, zaos)
            zaos.closeArchiveEntry()
        }
        zaos.close()

        then:
        outFile.exists()
        outFile.size() > 10

    }

}

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

/*
String outPath = options.exportDir
if (outPath) {
    log.info "Using exportDir: $outPath"
} else {
    log.warn "Could not find exportDir in options, defaulting to './'  "
    outPath = './'
}

File outDir = com.lucidworks.ps.Helper.getOrMakeDirectory(outPath)
File outFile = new File(outDir, "TA-sample.zip")


ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile))
ZipEntry e = new ZipEntry("objects.json")
out.putNextEntry(e)

byte[] data = json.getBytes()
out.write(data, 0, data.length)
//out.write();
out.closeEntry()
out.close()
log.info "wrote file: ${outFile.absolutePath}"
*/

