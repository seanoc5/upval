package misc.compare

import com.lucidworks.ps.compare.FusionApplicationComparator
import com.lucidworks.ps.fusion.Application
import org.apache.log4j.Logger
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/23/22, Monday
 * @description:
 */
final Logger log = Logger.getLogger(this.class.name);

log.info "Starting ${this.class.name}..."


File devSource = new File('/home/sean/Downloads/coppel_dev.zip')
Application devApp = new Application(devSource)
log.info "Left App:${devApp}"

File prodSource = new File('/home/sean/Downloads/coppel_prod.zip')
Application prodApp = new Application(prodSource)
log.info "Right App:${prodApp}"

FusionApplicationComparator comparator = new FusionApplicationComparator(devApp, prodApp)
log.info "Results: ${comparator}"

File outfile = new File(devSource.parentFile, 'compare.dev-prod.coppel.json')
//JsonBuilder builder = new JsonBuilder(comparator)
//outfile.text = builder.toPrettyString()
//log.info "wrote output file: $outfile.absolutePath"

log.info "Done...?"
