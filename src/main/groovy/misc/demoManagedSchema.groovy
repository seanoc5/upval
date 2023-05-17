package misc

import com.lucidworks.ps.model.solr.ManagedSchema
import groovy.xml.XmlUtil

/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   6/13/22, Monday
 * @description:
 */
// todo move this to a test...
import org.apache.log4j.Logger

final Logger log = Logger.getLogger(this.class.name);

log.info "Starting ${this.class.name}..."
def schemaSource = getClass().getResource('/examples/f4.basic.managed-schema.xml')
def lukeSource = getClass().getResource('/examples/f4.luke-output-basic.json')

ManagedSchema schema = new ManagedSchema(schemaSource, schemaSource.toString(), lukeSource)
def dynamicFields = schema.schemaDynamicFieldDefinitions
def unusedDynamic = schema.findUnusedDynamicfields()
def results = schema.removeUnusedDynamicFields(unusedDynamic)

def unusedFieldTypes = schema.findUnusedFieldTypes()
def results2 = schema.removeUnusedFieldsTypes(unusedFieldTypes)
def xmlpretty = XmlUtil.serialize(schema.xmlSchema)
log.info "Original content size: (${schema.content.size()} --> trimmed:  ${xmlpretty.size()}"
log.info "trimmed xml:\n$xmlpretty"

//def xmlOutput = new StringWriter()
//def xmlNodePrinter = new NodePrinter(new PrintWriter(xmlOutput))
//xmlNodePrinter.print(schema.xmlSchema)
log.info "Done...?"
