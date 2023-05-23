//package misc.experiment
//
//import groovy.xml.XmlParser
//import org.apache.log4j.Logger
//import org.xmlunit.builder.DiffBuilder
//import org.xmlunit.builder.Input
//import org.xmlunit.diff.DOMDifferenceEngine
//import org.xmlunit.diff.Diff
//import org.xmlunit.diff.Difference
//import org.xmlunit.diff.DifferenceEngine
//
///**
// * demo script exploring XmlUnit for xml diffs
// * SoC: I found it too verbose and general, but worth keeping here in for general interest/review?
// */
//Logger log = Logger.getLogger(this.class.name);
//log.info "Starting ${this.class.name}"
//
//XmlParser parser = new XmlParser()
//
//def leftUrl = getClass().getClassLoader().getResource("templates/configsets/fusion-3.1.5/basic_configs/conf/managed-schema")
//def rightUrl = getClass().getClassLoader().getResource("templates/configsets/fusion-4.2.6/_default/conf/managed-schema")
//
//def leftObject = Input.fromURL(leftUrl)
//def rightObject = Input.fromURL(rightUrl)
//DifferenceEngine diff = new DOMDifferenceEngine();
//
//Diff myDiff = DiffBuilder.compare(leftObject)
//        .withTest(rightObject)
//        .ignoreWhitespace()
//        .ignoreComments()
//        .normalizeWhitespace()
//        .build()
//
//Iterator<Difference> iter = myDiff.getDifferences().iterator();
//iter.each { Difference d ->
//    log.info "Difference: $d (control: ${d.comparison.controlDetails}) -- (${d.comparison.testDetails})"
//}
//
//log.info "done..."
