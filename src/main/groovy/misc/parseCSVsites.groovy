package misc

import com.lucidworks.ps.clients.FusionClient
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.apache.log4j.Logger

import java.nio.file.Path
import java.nio.file.Paths

Logger log = Logger.getLogger(this.class.name);
log.info "Start ${this.class.name}..."

// todo -- replace me
File csvFile = new File('../../data/Support Passwords-Stage.csv')
Reader reader = csvFile.newReader()

Iterable<CSVRecord> csvRecord = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(reader);
for (CSVRecord record : csvRecord) {
    String customer = record.get("Customer");
    String type = record.get("Type");
    String url = record.get("URL");
    String user = record.get("UserName");
    String pass = record.get("Password");
    log.info "user: $user"

    log.info "${csvRecord.recordNumber}) ${customer} -- ${url}"
    if(user && pass) {
        if (url.trim()) {
            if (url.startsWith('http')) {
                log.debug "Found proper url with schema: $url"
            } else {
                log.info "No schema found? $url, defaulting to https..."
                url = "https://$url"
            }

            FusionClient fusionClient = new FusionClient(new URL(url), user, pass)

            def apps = fusionClient.getApplications()
            log.info "$customer) Applications: $apps"
            apps.each { Map appObj ->
                String appId = appObj.id
                log.info "\t\tApp ID: ${appId}"

                Path outPath = Paths.get("../replaceme/data/${customer}.app.${appId}.zip")
                def exp = fusionClient.exportFusionObjects("app.ids=${appId}", outPath)
                log.debug "do something with export: $exp"
            }

        } else {
            log.debug "Skip empty url... $values"
        }
    } else {
        log.warn "Missing user:($user) or pass:$pass"
    }
}

log.info "Done...?"
