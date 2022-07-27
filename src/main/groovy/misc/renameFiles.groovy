package misc

import groovy.io.FileType
import groovy.transform.Field
import org.apache.log4j.Logger

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   7/26/22, Tuesday
 * @description:
 */

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

@Field
final Logger log = Logger.getLogger(this.class.name);
log.info "Starting ${this.class.name}..."



def rules = null
//if(options.config) {
    File cfgFile = new File('/home/sean/work/lucidworks/upval/src/test/resources/configurations/deployDCLargeConfiguration.groovy')
    ConfigObject config = new ConfigSlurper().parse(cfgFile.toURI().toURL())
    rules = config.rules
//} else {
//    throw new IllegalArgumentException("No config file given")
//}



Path srcFolder = Paths.get('/home/sean/Downloads/DC_Large')
//File dest = Helper.getOrMakeDirectory('/home/sean/Downloads/AcmeDigicommerce')
//Path destFolder = dest.toPath()
srcFolder.eachFileRecurse(FileType.FILES) { Path path ->
    log.info "Path: '$path'"
    def changes = applyRenameRules(rules, path)
    if(changes) {
        log.info "Changes: $changes"
    } else {
        log.debug "\t\tno changes"
    }
}

def applyRenameRules(rules, Path path) {
    List<String> changes = []
    List<Path> foldersToRename = []
    String fname = path.fileName
    boolean match = false
    rules.rename.each { Map rule ->
        def namePattern = rule.namePattern
        def replacement = rule.replacement
        if (namePattern instanceof String) {
            if (fname.contains(namePattern)) {
                String renamed = fname.replaceAll(namePattern, replacement)
                Path movedPath = Files.move(path, path.resolveSibling(renamed))
                log.info "\t\tRename file: $fname as it matches '$namePattern' using replacement value: $replacement to get: $renamed -> $movedPath"
                changes << "$fname -> $renamed"
                match = true
                path = movedPath            // update the source path, in case we have multiple rename/moves....
            } else {
                log.debug "\t\t\t\tno name match for namePattern:$namePattern on filename:$fname"
            }
        } else if (namePattern instanceof Pattern) {
            Matcher matcher = fname =~ namePattern
            if (matcher.matches()) {
                String renamed = fname.replaceAll(namePattern, replacement)
                log.info "Regex matching: $fname matches pattern: $namePattern"
                changes << "$fname -> $renamed"
                match = true
            } else {
                log.debug "\t\t\t\tNo regex match: $namePattern for file: $fname"
            }
        }
    }
    return changes
}

log.info "Done...?"
