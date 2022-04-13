package com.lucidworks.ps.upval.mapping

import org.apache.log4j.Logger

class SharepointOptimizedTransformer extends ObjectTransformer {
    Logger log = Logger.getLogger(this.class.name);

    SharepointOptimizedTransformer(Map srcMap, Map destMap, Map transConfig, String separator) {
        super(srcMap, destMap, transConfig, separator)
    }

    def test(String templatePath, String sourcePath, Map templateObject, Map sourceObject) {
        log.info "Testing 'transform' function (more to come): $sourcePath -> $templatePath (not showing objects...)"
        def src = getValueByMapPath(sourcePath, sourceObject)
        def destVal = getValueByMapPath(templatePath, templateObject)
        def rc = setByMapPath(templatePath, src, templateObject)
    }

    def startLinks(String templatePath, String sourcePath, Map templateObject, Map sourceObject) {
        log.info "Testing 'transform' function (more to come): $sourcePath -> $templatePath (not showing objects...)"
        def src = getValueByMapPath(sourcePath, sourceObject)
        def destVal = getValueByMapPath(templatePath, templateObject)
        def rc = setByMapPath(templatePath, src, templateObject)
    }
}
