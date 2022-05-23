package misc

import groovy.xml.XmlParser
import org.apache.log4j.Logger
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import org.xmlunit.diff.*

import java.nio.file.Path
import java.nio.file.Paths

Logger log = Logger.getLogger(this.class.name);
log.info "Starting ${this.class.name}"

XmlParser parser = new XmlParser()

// todo -- revisit paths, these are broken at the moment
Path templateFolder = Paths.get('../resources/templates/configsets/7.7.2/_default/conf')
Path tsPath = Paths.get(templateFolder.toAbsolutePath().toString(), 'managed-schema')
log.info "Template schema: $tsPath -- ${tsPath.toFile().exists()}"
log.info "Template path abs: ${templateFolder.toAbsolutePath()}"

String depPath = "./data/"
Path deployedConfigFolder = Paths.get(depPath)
File dsFile = new File('./configsets/managed-schema')
log.info "Deployed schema: ${dsFile.absolutePath}"

def control = Input.fromPath(tsPath).build();
def test = Input.fromFile(dsFile).build();
DifferenceEngine diff = new DOMDifferenceEngine();

Diff myDiff = DiffBuilder.compare(control)
        .withTest(test)
        .ignoreWhitespace()
        .ignoreComments()
        .normalizeWhitespace()
        .build()

Iterator<Difference> iter = myDiff.getDifferences().iterator();
iter.each { Difference d ->
    log.info "Difference: $d (control: ${d.comparison.controlDetails}) -- (${d.comparison.testDetails})"
}

log.info "done..."
