package com.lucidworks.ps.mapping

import org.apache.log4j.Logger

class LdapAclsTransformer extends ObjectTransformerJayway {
    Logger log = Logger.getLogger(this.class.name);

    LdapAclsTransformer(Map srcMap, Map destMap, Map transConfig, String separator= '/') {
        super(srcMap, destMap, transConfig, separator)
        log.info "Construct new LdapAclsTransformer..."
    }


}
