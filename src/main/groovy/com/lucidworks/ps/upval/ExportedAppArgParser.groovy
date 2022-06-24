package com.lucidworks.ps.upval

import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor

/**
 * Helper class to standardize argument syntax for FusionClient programs and driver-scripts
 */
class ExportedAppArgParser {

    public static OptionAccessor parse(String toolName, String[] args) {
        CliBuilder cli = new CliBuilder(usage: "${toolName}.groovy -fhttp://myFusion5addr:6764 -uadmin -psecret123 -s~/data/MyApp.objects.json -m ~/Fusion/migration/F4/mappingFolder", width: 160)
        cli.with {
            h longOpt: 'help', 'Show usage information'
            e longOpt: 'exportDir', args: 1, required: false, argName: 'dir', 'Export directory'
            s longOpt: 'source', args: 1, required: false, argName: 'sourceFile', 'Source (objects.json or appexport.zip) to read application objects from (old app to be migrated)'
        }


        OptionAccessor options = cli.parse(args)
        if (!options) {
            System.exit(-1)
        }
        if (options.help) {
            cli.usage()
            System.exit(0)
        }
        options
    }

}
