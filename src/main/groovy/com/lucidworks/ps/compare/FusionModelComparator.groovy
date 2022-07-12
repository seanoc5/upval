package com.lucidworks.ps.compare

import com.lucidworks.ps.model.fusion.Application
import org.apache.log4j.Logger
/**
 * @author :    sean
 * @mailto :    seanoc5@gmail.com
 * @created :   5/23/22, Monday
 * @description: helper class to compare two different Fusion Applications (or model parts of)
 * replaces FusionApplicationComparator
 */
class FusionModelComparator {
    Logger log = Logger.getLogger(this.class.name);

    Application leftApp
    Application rightApp

    Map<String, CompareCollectionResults> collectionComparisons = [:]
    Map<String, CompareJsonObjectResults> objectComparisons = [:]

    public static final List<String> DEFAULTTHINGSTOCOMPARE = "configsets collections dataSources indexPipelines queryPipelines parsers blobs appkitApps features objectGroups links sparkJobs".split(' ')

    FusionModelComparator(Application left, Application right, List<String> thingsToCompare = [], def ignorePatterns = /.*created.*/) {
        if (!thingsToCompare) {
            thingsToCompare = DEFAULTTHINGSTOCOMPARE
            log.info "Using default list of things to compare: $thingsToCompare"
        } else {
            log.info "Using list of things to compare from param: $thingsToCompare"
        }
        log.info "Comparing [left] App: (${left}) ==with== (${right}) app..."
        this.leftApp = left
        this.rightApp = right
        compare(thingsToCompare, ignorePatterns)
    }

    /**
     * iterate through different things in both left and right app, compare the groups of things, and then also compare each 'shared' thing for differences
     * @param thingsToCompare
     * @return
     */
    Map<String, CompareCollectionResults> compare(List<String> thingsToCompare, def valueDiffsToIgnore) {
        log.info "Compare things matching: (${thingsToCompare}) -- Value Diffs to ignore: $valueDiffsToIgnore "

        thingsToCompare.each { String thingType ->
            log.info "Compare things of type: $thingType"
            Object leftThings = leftApp.getThings(thingType)
            Object rightThings = rightApp.getThings(thingType)

            BaseComparator comparator = new BaseComparator(thingType, leftThings, rightThings, valueDiffsToIgnore)
            def results = comparator.compare()
            log.info "$thingType) comparator: $comparator"
        }

        return collectionComparisons
    }



}


