package com.lucidworks.ps.upval.mapping

import org.apache.log4j.Logger

class LdapAclsTransformer extends ObjectTransformer {
    Logger log = Logger.getLogger(this.class.name);

    LdapAclsTransformer() {
        log.info "Construct new LdapAclsTransformer..."
    }

    def test(String templatePath, String sourcePath, Map templateObject, Map sourceObject) {
        log.info "Testing 'transform' function (more to come): $sourcePath -> $templatePath (not showing objects...)"
        def src = getByMapPath(sourcePath, sourceObject)
        def destVal = getByMapPath(templatePath, templateObject)
        def rc = setByMapPath(templatePath, src, templateObject)
    }

    def startLinks(String templatePath, String sourcePath, Map templateObject, Map sourceObject) {
        log.info "Testing 'transform' function (more to come): $sourcePath -> $templatePath (not showing objects...)"
        def src = getByMapPath(sourcePath, sourceObject)
        def destVal = getByMapPath(templatePath, templateObject)
        def rc = setByMapPath(templatePath, src, templateObject)
    }
}
