package com.lucidworks.ps.transform

import org.apache.log4j.Logger

/**
 * stub code to flesh out for transforming LDAPAcls (which tend to need moderate transforming)
 * todo complete me...
 */
class LdapAclsTransformer extends ObjectTransformerJayway {
    Logger log = Logger.getLogger(this.class.name);

    LdapAclsTransformer(Map srcMap, Map destMap, Map transConfig, String separator= '/') {
        super(srcMap, destMap, transConfig, separator)
        log.info "Construct new LdapAclsTransformer..."
    }


}
