package misc

import com.lucidworks.ps.Helper
import com.lucidworks.ps.clients.DeploymentArgParser
import com.lucidworks.ps.model.fusion.Application
import groovy.cli.picocli.OptionAccessor
import groovy.transform.Field
import org.apache.log4j.Logger

import java.nio.file.*
import java.util.regex.Matcher
import java.util.regex.Pattern
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   7/26/22, Tuesday
 * @description:
 */
@Field
final Logger log = Logger.getLogger(this.class.name);
log.info "Starting ${this.class.name}..."

OptionAccessor options = DeploymentArgParser.parse(this.class.name, args)
File appZip = new File(options.source)
File exportDir = null
if(options.exportDir){
    exportDir = Helper.getOrMakeDirectory( new File(options.exportDir))
} else {
    log.info "No export dir given, not saving assessment..."
}

Application app = new Application(appZip)
Map srcMap =  Application.getObjectsJsonMap(appZip)



def copyOption = StandardCopyOption.REPLACE_EXISTING
boolean cleanDestination = true

Map<String, List<Map>> rules = null
//if(options.config) {
File cfgFile = new File('/home/sean/work/lucidworks/upval/src/test/resources/configs/deployDCLargeConfiguration.groovy')
ConfigObject config = new ConfigSlurper().parse(cfgFile.toURI().toURL())
rules = config.rules
//} else {
//    throw new IllegalArgumentException("No config file given")
//}


Path srcFolder = Paths.get('/home/sean/Downloads/DC_Large')
File dest = new File('/home/sean/Downloads/AcmeDigicommerce')
if(dest && dest.exists() && cleanDestination){
    boolean success = dest.deleteDir()
    if(success) {
        log.info "Cleaned destination dir: $dest based on arg: cleanDestination:$cleanDestination"
    } else {
        log.error "Could not delete/clean destination dir: $dest -- most likely this will create problems...."
    }
}
dest = Helper.getOrMakeDirectory('/home/sean/Downloads/AcmeDigicommerce')


Path destFolder = dest.toPath()
srcFolder.eachFileRecurse { Path srcItem ->
    log.info "Path: '$srcItem'"
    def changes = applyRenameRules(rules, srcFolder, srcItem, destFolder, copyOption)
    if (changes) {
        log.info "\t\tChanges: $changes"
    } else {
        log.debug "\t\tno changes"
    }
}

def applyRenameRules(Map<String, List<Map>> rules, Path srcFolder, Path srcItem, Path destFolder, CopyOption copyOption) {
    List<String> changes = []
    // get the relative name/path for use in copy operation below
    Path relativePath = srcFolder.relativize(srcItem)
    String srcPath = srcItem.toString()
//    String fname = relativePath
    String destPath = relativePath
    rules.skip.each { def skipRule ->
        if (destPath) {
            if (skipRule instanceof String) {
                if (srcPath.contains(skipRule)) {
                    log.info "\t\tSkipping srcItem:($srcItem) because it contains skip rule text: $skipRule"
                    changes << "$srcPath -> skip based on String contains rule: $skipRule"
                    destPath = null
                }
            } else if (skipRule instanceof Pattern) {
                if (srcPath ==~ skipRule) {
                    log.info "\t\tSkipping srcItem:($srcItem) because it matches skip rule: $skipRule"
                    changes << "$srcPath -> skip based on regex match rule: $skipRule"
                    destPath = null
                }
            } else {
                log.warn "Unknown skip rule:($skipRule) type: ${skipRule.getClass().simpleName}"
            }

        } else {
            log.debug "\t\t we seem to have found a previous skip rule, short-circuiting remaining rules, and should skip this file altogether: $srcItem"
        }
    }
    if (destPath) {
        rules.rename.each { Map rule ->
            def namePattern = rule.namePattern
            def replacement = rule.replacement
            if (namePattern instanceof String) {
                if (destPath.contains(namePattern)) {
                    destPath = destPath.replaceAll(namePattern, replacement)
                    log.info "String srcPath:($srcPath) contains pattern:($namePattern) -> replaceAll to:$destPath :: Rule:$rule"
                    changes << "$srcPath -> $destPath :: Rule:$rule"
                } else {
                    log.debug "\t\t\t\tno name match for namePattern:$namePattern on filename:$destPath"
                }
            } else if (namePattern instanceof Pattern) {
                if(replacement.contains('$')){
                    Matcher matcher =( destPath =~ namePattern)
                    if (matcher.matches()) {
                        destPath = destPath.replaceAll(namePattern, replacement)
                        log.info "Found what looks like a capture group in the replacement:$replacement -- going full regex matcher"
                        log.info "Regex matching: $destPath matches pattern: $namePattern"
                        changes << "$srcPath -> $destPath :: Rule:$rule"
                    } else {
                        log.debug "\t\t\t\tNo regex match: $namePattern for file: $destPath"
                    }
                } else {
                    Matcher matcher = (destPath =~ namePattern)
                    if (matcher.find()) {
                        destPath = destPath.replaceAll(namePattern, replacement)
                        changes << "$srcPath -> $destPath :: Rule:$rule"
                        log.info "The replacement:$replacement does not seem to have any capture groups, doing simple regex namePattern:($namePattern) in destPath:$destPath)"
//                        log.info "Regex matching: $destPath matches pattern: $namePattern"
                    } else {
                        log.debug "\t\t\t\tNo regex match: $namePattern for srcPath: $srcPath"
                    }
                }
            }
        }
        if (destPath) {
            log.info "\t\tUsing transformed destination path:$destPath, from srcItem: $srcItem :: Rule:$rule"
        } else {
            destPath = relativePath
            log.info "\t\tUsing source path ($relativePath) relative to srcFolder:($srcFolder) for destination path:$destPath ($destFolder), no copy/transform rules matched :: Rule:$rule"
        }

        Path destItem = destFolder.resolve(destPath)
//    Path destItem = destFolder.relativize(destPath)
        def rc = Files.copy(srcItem, destItem, copyOption)
        log.debug "\t\tCopied srcItem:$srcItem to destItem:$destItem -- result code: $rc"
    } else {
        log.debug "\t\tSkipped item based on matching skip rule..."
    }
    return changes
}

log.info "Done...?"
