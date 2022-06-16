package com.lucidworks.ps.upval

import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor

/**
 * Helper class to standardize argument syntax for FusionClient programs and driver-scripts
 */
class FusionClientArgParser {

    public static OptionAccessor parse(String toolName, String[] args) {
        CliBuilder cli = new CliBuilder(usage: "${toolName}.groovy -fhttp://myFusion5addr:6764 -uadmin -psecret123 -s~/data/MyApp.objects.json -m ~/Fusion/migration/F4/mappingFolder", width: 160)
        cli.with {
            h longOpt: 'help', 'Show usage information'
            a longOpt: 'appName', args: 1, required: false, argName: 'AppName', 'Application name to work on (optional, but required for some operations...)'
            e longOpt: 'exportDir', args: 1, required: false, argName: 'dir', 'Export directory'
            f longOpt: 'fusionUrl', args: 1, required: true, argName: 'url', 'MAIN/Destination Fusion url with protocol, host, and port (if any)--for new/migrated app'
            g longOpt: 'groupLabel', args: 1, required: false, argName: 'group', defaultValue: 'TestGroup', 'Label for archiving/grouping objects; app name, environment, project,... freeform and optional'
            m longOpt: 'mappingDir', args: 1, required: false, argName: 'dir', 'Folder containing object mapping instructions (subfolders grouped by object type)'
            p longOpt: 'password', args: 1, required: true, argName: 'passwrd', 'password for authentication in fusion cluster (assuming basicAuth for now...) for MAIN/dest fusion'
            s longOpt: 'source', args: 1, required: false, argName: 'sourceFile', 'Source (objects.json or appexport.zip) to read application objects from (old app to be migrated)'
            u longOpt: 'user', args: 1, argName: 'user', required: true, 'the fusion user to authenticate with for MAIN/dest fusion'

            w longOpt: 'whichApp', args: 1, required: false, argName: 'srcApp', 'Which SOURCE app to read from (if different from "appName"), eg /opt/lucidworks/fusion/exports/myApp.zip'
            x longOpt: 'srcFusionUrl', args:1, required: false, argName: 'url', 'the fusion url to read from SOURCE fusion (read only), eg https://my.fusion.com:8764/'
            y longOpt: 'srcUser', args:1, required: false, argName: 'user', 'the username to authenticate to SOURCE fusion (read only)'
            z longOpt: 'srcPass', args:1, required: false, argName: 'password', 'the password to authenticate to SOURCE fusion (read only)'
        }


        OptionAccessor options = cli.parse(args)
        if (!options) {
//            cli.usage()
            System.exit(-1)
        }
        if (options.help) {
            cli.usage()
            System.exit(0)
        }
        options
    }

}
