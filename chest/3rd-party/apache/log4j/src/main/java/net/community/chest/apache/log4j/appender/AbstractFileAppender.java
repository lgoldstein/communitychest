package net.community.chest.apache.log4j.appender;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;

import net.community.chest.apache.log4j.AbstractAppender;
import net.community.chest.apache.log4j.appender.jmx.AbstractFileAppenderController;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Base implementation for {@link org.apache.log4j.Appender}-s that write to file(s)</P>
 *
 * @author Lyor G.
 * @since Sep 30, 2007 11:58:02 AM
 */
public abstract class AbstractFileAppender extends AbstractAppender implements AbstractFileAppenderController, Closeable {
    protected AbstractFileAppender ()
    {
        super();
    }

    private File    _logDir    /* =null */;
    /**
     * @return root folder directory under which each "application" should
     * open its own sub-folder
     */
    public File getLogDir ()
    {
        return _logDir;
    }

    public void setLogDir (File logDir)
    {
        _logDir = logDir;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#getLogPath()
     */
    @Override
    public String getLogPath ()
    {
        final File    logDir=getLogDir();
        return (null == logDir) ? null : logDir.getAbsolutePath();
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#setLogPath(java.lang.String)
     */
    @Override
    public void setLogPath (String logPath)
    {
        setLogDir(((null == logPath) || (logPath.length() <= 0)) ? null : new File(logPath));
    }
    /**
     * @return Current {@link File} used for logging - may be null
     */
    public abstract File getCurrentLogFile ();
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#getCurrentLogFilePath()
     */
    @Override
    public String getCurrentLogFilePath ()
    {
        final File    f=getCurrentLogFile();
        if (null == f)
            return null;
        return f.getAbsolutePath();
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#getCurrentFileSize()
     */
    @Override
    public long getCurrentFileSize ()
    {
        final File    f=getCurrentLogFile();
        if (null == f)    // OK
            return 0;

        try
        {
            return f.length();
        }
        catch(Exception e)
        {
            errorReport("getCurrentFileSize(" + f + ")", e, (-1));
            return (-1L);
        }
    }

    private String    _extension=".log";
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#getExtension()
     */
    @Override
    public String getExtension ()
    {
        return _extension;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#setExtension(java.lang.String)
     */
    @Override
    public void setExtension (String extension)
    {
        _extension = extension;
    }

    private class ByExtensionFilter implements FileFilter {
        protected ByExtensionFilter ()
        {
            super();
        }
        /*
         * @see java.io.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept (File pathname)
        {
            if ((null == pathname) || pathname.isDirectory())
                return false;

            final String    ext=getExtension();
            if ((null == ext) || (ext.length() <= 0))
                return true;    // accept everything if no specific extension

            final String    path=pathname.getAbsolutePath();
            final int        pLen=(null == path) /* should not happen */ ? 0 : path.length(),
                            ePos=(pLen <= 1) /* should not happen */ ? (-1) : path.lastIndexOf('.');
            final String    pxt=((ePos <= 0) || (ePos >= (pLen-1))) ? null : path.substring(ePos);
            // NOTE !!! we check case insensitive
            if (ext.equalsIgnoreCase(pxt))
                return true;    // just so we have a debug breakpoint

            return false;
        }
    }
    /**
     * @return {@link FileFilter} to use in order to determine which are
     * valid log files and which are not (null == all). Default is all
     * files that have the specified extension
     */
    public FileFilter getLogFilesFilter ()
    {
        return new ByExtensionFilter();
    }

    private int    _IOBufSize /* =0 */;
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#getIOBufSizeKB()
     */
    @Override
    public int getIOBufSizeKB ()
    {
        return _IOBufSize;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#setIOBufSizeKB(int)
     */
    @Override
    public void setIOBufSizeKB (int bufSize)
    {
        _IOBufSize = bufSize;
    }
}
