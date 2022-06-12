package misc.compare

import com.lucidworks.ps.compare.SolrConfigComparator
import com.lucidworks.ps.upval.SolrComparatorArgParser
import groovy.cli.picocli.OptionAccessor
import groovy.xml.XmlParser
import org.apache.log4j.Logger

import java.nio.file.Path
import java.nio.file.Paths
// deprecated -- old approach -- remove me?
Logger log = Logger.getLogger(this.class.name);
log.info "Starting ${this.class.name}"


OptionAccessor options = SolrComparatorArgParser.parse(this.class.name, args)

XmlParser parser = new XmlParser()
def leftSource = options.left
def rightSource = options.right

Path leftPath = Paths.get(leftSource)
Path rightPath = Paths.get(rightSource)
log.info "Left: : $leftPath -- ${leftPath.toFile().exists()}"
log.info "Right: : $rightPath -- ${rightPath.toFile().exists()}"

Node leftSchema = parser.parse(leftSource)
Node rightSchema = parser.parse(rightSource)

def results = SolrConfigComparator.compareXmlObjects(leftSchema, rightSchema)

log.info "done..."

