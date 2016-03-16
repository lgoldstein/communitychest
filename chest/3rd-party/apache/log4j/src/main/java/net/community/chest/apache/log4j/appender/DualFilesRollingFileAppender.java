package net.community.chest.apache.log4j.appender;

import java.io.File;

import org.apache.log4j.spi.LoggingEvent;

import net.community.chest.io.FileUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses a &quot;primary&quot; file for logging, and renames it to a
 * backup one every time re-open is required</P>
 *
 * @author Lyor G.
 * @since Apr 27, 2008 2:36:33 PM
 */
public class DualFilesRollingFileAppender extends AbstractRollingFileAppender {
    public DualFilesRollingFileAppender ()
    {
        super();
    }
    private long    _openTime    /* =0L */, _fileSize    /* =0L */;
    /*
     * @see net.community.chest.apache.log4j.appender.AbstractRollingFileAppender#getCurrentFileOpenTime()
     */
    @Override
    public long getCurrentFileOpenTime ()
    {
        return _openTime;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.AbstractFileAppender#getCurrentFileSize()
     */
    @Override
    public long getCurrentFileSize ()
    {
        return _fileSize;
    }

    public static final String    DEFAULT_BACKUP_FILE_EXTENSION=".bak";
    private String    _bakFileExtension    /* =null */;
    public String getBackupFileExtension ()
    {
        if ((null == _bakFileExtension) || (_bakFileExtension.length() <= 0))
            return DEFAULT_BACKUP_FILE_EXTENSION;
        return _bakFileExtension;
    }

    public void setBackupFileExtension (String ext)
    {
        _bakFileExtension = ext;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.AbstractRollingFileAppender#buildBaseLogFileName()
     */
    @Override
    protected String buildBaseLogFileName ()
    {
        return getFileNamePrefix();
    }
    /*
     * @see net.community.chest.apache.log4j.appender.AbstractRollingFileAppender#assignLoggingFile()
     */
    @Override
    protected File assignLoggingFile ()
    {
        final String    baseFileName=buildBaseLogFileName(),
                        fileExt=FileUtil.adjustExtension(getExtension(), true),
                        fileName=baseFileName + fileExt,
                        bakExt=FileUtil.adjustExtension(getBackupFileExtension(), true),
                        bakName=fileName + bakExt;
        final File        logDir=getLogDir(),
                        logFile=new File(logDir, fileName),
                        bakFile=new File(logDir, bakName);
        if (bakFile.exists())
        {
            if (!bakFile.delete())
                errorReport("assignLoggingFile() failed to delete backup file=" + bakFile);
        }

        if (logFile.exists())
        {
            if (!logFile.renameTo(bakFile))
                errorReport("assignLoggingFile() failed to rename to backup file=" + bakFile);
        }

        // re-start from scratch
        _fileSize = 0L;
        _openTime = System.currentTimeMillis();

        return logFile;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.AbstractRollingFileAppender#appendFormattedEvent(org.apache.log4j.spi.LoggingEvent, java.lang.String)
     */
    @Override
    protected boolean appendFormattedEvent (LoggingEvent e, String msg)
    {
        if (super.appendFormattedEvent(e, msg))
        {
            final long    msgLen=(null == msg) ? 0 : msg.length();
            if (msgLen > 0)    // we do not count CR/LF(s)...
                _fileSize += msgLen;

            return true;
        }

        return false;
    }
}
