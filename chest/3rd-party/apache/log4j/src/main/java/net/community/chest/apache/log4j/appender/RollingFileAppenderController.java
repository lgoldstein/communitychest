package net.community.chest.apache.log4j.appender;

import java.util.Collection;

import net.community.chest.apache.log4j.appender.jmx.AbstractFileAppenderController;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Can be used to generate MBean with useful attributes and operations
 * related to an {@link AbstractRollingFileAppender}</P>
 *
 * @author Lyor G.
 * @since Oct 1, 2007 2:10:40 PM
 */
public interface RollingFileAppenderController extends AbstractFileAppenderController {
    /**
     * @return Max. file size (KB) before closing it and opening a new one. If
     * non-positive than no limit imposed.
     */
    int getMaxSizeKB ();
    void setMaxSizeKB (int maxSizeKB);
    /**
     * @return Max. age (days) for deleting old log files whenever a new one
     * is opened (if non-positive, then feature is disabled)
     */
    int getMaxAgeDays ();
    void setMaxAgeDays (int d);
    /**
     * @return TRUE=close current file at midnight even if max. size/age not
     * reached yet. FALSE=do not close the file at midnight
     */
    boolean isRollAtMidnight ();
    void setRollAtMidnight (boolean rollAtMidnight);
    /**
     * @return current "index" value used to distinguish between files created
     * in the same day.
     */
    String getCurrentFileIndex ();
    /**
     * @return Prefix to be used before the date component in the generated
     * file name(s)
     */
    String getFileNamePrefix ();
    void setFileNamePrefix (String prfx);
    /**
     * @return Last modify time difference (msec.) so that an existing previous
     * log file will be re-used (appended to) - feature disabled if non-positive
     */
    long getAppendTimeDiff ();
    void setAppendTimeDiff (long appendTimeDiff);
    /**
     * Closes currently open file (if any). It can also optionally open a new
     * file (or leave it to the next logging event to trigger it) and/or
     * delete older files
     * @return TRUE if a file was closed
     */
    boolean rollOver ();
    /**
     * Checks which files have exceeded the specified max. age (hours) and
     * deletes them accordingly
     * @param maxAgeHours max. age (hours) - ignored if non-positive
     * @return {@link Collection} of all file paths successfully deleted
     * (null/empty if none deleted or feature disabled)
     */
    Collection<String> removeOldFiles (int maxAgeHours);
    /**
     * Checks which files have exceeded the default max. age and deletes them
     * @return {@link Collection} of all file paths successfully deleted
     * (null/empty if none deleted or feature disabled)
     */
    Collection<String> removeOldFiles ();
}
