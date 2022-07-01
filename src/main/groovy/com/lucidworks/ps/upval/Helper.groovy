package com.lucidworks.ps.upval

import org.apache.log4j.Logger

import java.text.DateFormat

//import java.security.InvalidParameterException

import java.text.SimpleDateFormat
/**
 * General helper class
 * todo consider refactoring flattening operations to a more obvious classname (low-priority as it is a low-level op, not big picture processing)
 */
class Helper {
    static Logger log = Logger.getLogger(this.class.name);

    /**
     * util function to get an (output) folder (for exporting), create if necessary
     * @param dirPath where the (new) directory/folder should be
     * @return created directory
     */
    static File getOrMakeDirectory(String dirPath) {
        File folder = new File(dirPath)
        if (folder.exists()) {
            if (folder.isDirectory()) {
                log.debug "Folder (${folder.absolutePath} exists, which is good"
            } else {
                log.warn "Folder (${folder.absolutePath}) exists, but is not a folder, which is bad"
                throw new IllegalAccessException("Job Folder (${folder.absolutePath}) exists, but is not a folder, which is bad, aborting")
            }
        } else {
            def success = folder.mkdirs()
            if (success) {
                log.info "\t\tCreated folder: ${folder.absolutePath}"
            } else {
                log.warn "Folder (${folder.absolutePath}) could not be created, which is bad"
                throw new IllegalAccessException("Folder (${folder.absolutePath}) exists, could not be created which is bad, aborting")
            }
        }
        folder
    }

/**
 * placeholder for getting a psuedo source-control folder name for exports (and potentially imports / restore)
 * @param date
 * @param dateFormat --
 * @return a "sort friendly" datestamp with hour & minute to allow multiple snapshots per day (or per hour)...
 */
    static String getVersionName(Date date = new Date(), DateFormat dateFormat = new SimpleDateFormat('yyyy-MM-dd.hh.mm')) {
        String s = dateFormat.format(date)
    }


}

