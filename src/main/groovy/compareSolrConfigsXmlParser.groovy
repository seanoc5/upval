import com.lucidworks.ps.compare.ComparisonResult
import com.lucidworks.ps.compare.SolrConfigComparator as SCC
import groovy.xml.XmlParser
import org.apache.log4j.Logger

import java.nio.file.Path
import java.nio.file.Paths

Logger log = Logger.getLogger(this.class.name);
log.info "Starting ${this.class.name}"

XmlParser parser = new XmlParser()

Path templateFolder = Paths.get('../resources/templates/configsets/7.7.2/_default/conf')
Path tsPath = Paths.get(templateFolder.toAbsolutePath().toString(), 'managed-schema')
log.info "Template schema: $tsPath -- ${tsPath.toFile().exists()}"
log.info "Template path abs: ${templateFolder.toAbsolutePath()}"

String depPath = "/home/sean/work/lucidworks/Intel/CircuitSearch/"
Path deployedConfigFolder = Paths.get(depPath)
File dsFile = new File('/home/sean/work/lucidworks/data/CircuitSearch/configsets/CircuitSearch/managed-schema')
log.info "Deployed schema: ${dsFile.absolutePath}"

Node templSchema = parser.parse(tsPath.toFile())
Node depSchema = parser.parse(dsFile)

ComparisonResult result = SCC.compareSchemaUniqueIds(templSchema,depSchema)
log.info "Unique ids comparison result: $result"
log.info "done..."
