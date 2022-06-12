package misc.compare

import com.lucidworks.ps.compare.FusionApplicationComparator
import com.lucidworks.ps.fusion.Application
import com.lucidworks.ps.upval.SolrComparatorArgParser
import groovy.cli.picocli.OptionAccessor
import org.apache.log4j.Logger

// deprecated -- old approach -- remove me?
Logger log = Logger.getLogger(this.class.name);
log.info "Starting ${this.class.name}"

OptionAccessor options = SolrComparatorArgParser.parse(this.class.name, args)


File leftSource = new File(options.left)
Application leftApp = new Application(leftSource)

File rightSource = new File(options.right)
Application rightApp = new Application(rightSource)

List<String> thingsToCompare = ['configsets']
FusionApplicationComparator comparator = new FusionApplicationComparator(leftApp, rightApp, thingsToCompare)
log.info "show Comparator results: ${comparator.toString()}"

log.info "done..."

