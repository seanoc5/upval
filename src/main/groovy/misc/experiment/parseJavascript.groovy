package misc.experiment

import org.apache.log4j.Logger

/**
 * sample script exploring how to usefully parse javascript and get functional blocks of code, or even just generalize each individual statement
 * e.g. var a= 'foo' ==> set a variable to a string
 * this seems jdk dependent, and packages are not visible (i.e. expects jdk 8, but project using 9+...?
 */



/*
import jdk.nashorn.internal.ir.Block
import jdk.nashorn.internal.ir.FunctionNode
import jdk.nashorn.internal.ir.Statement
import jdk.nashorn.internal.parser.Parser
import jdk.nashorn.internal.runtime.Context
import jdk.nashorn.internal.runtime.ErrorManager
import jdk.nashorn.internal.runtime.Source
import jdk.nashorn.internal.runtime.options.Options
import org.apache.log4j.Logger
*/



Logger log = Logger.getLogger(this.class.name)

/*
Options options = new Options("nashorn");
options.set("anon.functions", true);
options.set("parse.only", true);
options.set("scripting", true);

ErrorManager errors = new ErrorManager();
Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
String jscode = '''
var a = 10;
var b = a + 1;
function someFunction() {
    return b + 1;
}
'''
Source source   = Source.sourceFor  ("test", jscode);
//Source source   = new Source.("test", jscode);
Parser parser = new Parser(context.getEnv(), source, errors);
FunctionNode functionNode = parser.parse();
Block block = functionNode.getBody();
List<Statement> statements = block.getStatements();
*/

log.info "done...?"
