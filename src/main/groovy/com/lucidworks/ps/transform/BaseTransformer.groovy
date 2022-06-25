package com.lucidworks.ps.transform

/** Base class for transforming things
 * Assumes we are dealing with Fusion "object" like things, but this is still inn discovery (June 22)
 * todo-- decide if we keep this, augment it, or drop
 */
class BaseTransformer {
    def source
    def destination
    def rules

    /**
     * main action to perform series of transformations
     */
    def transform(){

    }
    def getSourceValue(def path){
        log.warn "more code here"
    }

    def getDestinationValue(def path){
        log.warn "more code here"
    }

    def getNode(def path){
        log.warn "more code here"
    }

    def getNodeParent(def path){
        log.warn "more code here"
    }

    def setSourceValue(def path, def valueToSet){
        log.warn "more code here"
    }


}
