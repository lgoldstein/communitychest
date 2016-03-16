package net.community.chest.apache.log4j.appender;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.io.LastModifiedTimeComparator;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.datetime.DateUtil;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Performs logging to a file - "rolling" it according to "cutoff"
 * parameters such as size, age, etc.</P>
 *
 * @author Lyor G.
 * @since Sep 30, 2007 12:01:55 PM
 */
public abstract class AbstractRollingFileAppender
        extends AbstractFileAppender
        implements RollingFileAppenderController {
    protected AbstractRollingFileAppender ()
    {
        super();
    }

    private int    _maxSizeKB    /* =0 */;
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#getMaxSizeKB()
     */
    @Override
    public int getMaxSizeKB ()
    {
        return _maxSizeKB;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#setMaxSizeKB(int)
     */
    @Override
    public void setMaxSizeKB (int maxSizeKB)
    {
        _maxSizeKB = maxSizeKB;
    }
    /**
     * @return TRUE if file should be rolled due to its size
     */
    public boolean isRollBySizeRequired ()
    {
        final int    maxSizeKB=getMaxSizeKB();
        if (maxSizeKB <= 0)    // OK if no limit on size
            return false;

        final long    curSize=getCurrentFileSize();
        if (curSize <= 0L)    // OK if no/empty current file
            return false;

        final int    curSizeKB=(int) (((curSize + 512L) >> 10) & 0x7FFFFFFF);
        if (curSizeKB >= maxSizeKB)
            return true;    // just so we have a debug breakpoint

        return false;
    }
    /**
     * Max. age (hours) for deleting old files when a new one is opened. If
     * non-positive the no such limit imposed
     */
    private int    _maxAgeHours    /* =0 */;
    public int getMaxAgeHours ()
    {
        return _maxAgeHours;
    }

    public void setMaxAgeHours (int maxAgeHours)
    {
        _maxAgeHours = maxAgeHours;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#removeOldFiles(int)
     */
    @Override
    public Collection<String> removeOldFiles (final int maxAgeHours)
    {
        if (maxAgeHours <= 0)
            return null;    // OK if feature disabled

        final File    logDir=getLogDir();
        if ((null == logDir) || (!logDir.exists()) || (!logDir.isDirectory()))
            return null;    // should not happen

        final FileFilter    ff=getLogFilesFilter();
        final File[]        toDelFiles=
            (null == ff) ? logDir.listFiles() : logDir.listFiles(ff);
        if ((null == toDelFiles) || (toDelFiles.length <= 0))
            return null;

        Collection<String>    remFiles=null;
        final long            maxAgeMsec=maxAgeHours * 3600L * 1000L,
                            nowMsec=System.currentTimeMillis();
        for (final File f : toDelFiles)
        {
            if (null == f)    // should not happen
                continue;

            try
            {
                if (f.isDirectory())
                    continue;    // ignore directories

                // we use last modified instead of created - but it is good enough
                final long    lastMod=f.lastModified();
                if (lastMod < 0L)    // should not happen
                    continue;

                final long    modDiff=(nowMsec - lastMod);
                if (modDiff < maxAgeMsec)
                    continue;

                if (!f.delete())
                    continue;
            }
            catch(Exception e)
            {
                errorReport("removeOldFiles(" + f + ")", e, (-1));
                continue;
            }

            final String    fp=f.getAbsolutePath();
            if ((null == fp) || (fp.length() <= 0))
                continue;    // should not happen

            if (null == remFiles)
                remFiles = new LinkedList<String>();
            remFiles.add(fp);
        }

        return remFiles;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#removeOldFiles()
     */
    @Override
    public Collection<String> removeOldFiles ()
    {
        return removeOldFiles(getMaxAgeHours());
    }
    /*
     * @see org.apache.log4j.spi.OptionHandler#activateOptions()
     */
    @Override
    public void activateOptions ()
    {
        try
        {
            removeOldFiles();
            super.activateOptions();
        }
        catch(Exception e)
        {
            errorReport("activateOptions() " + e.getClass().getName() + ": " + e.getMessage(), e, (-1));
        }
    }
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#getMaxAgeDays()
     */
    @Override
    public int getMaxAgeDays ()
    {
        return getMaxAgeHours() / 24;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#setMaxAgeDays(int)
     */
    @Override
    public void setMaxAgeDays (int d)
    {
        setMaxAgeHours(d * 24);
    }

    private boolean    _rollAtMidnight    /* =false */;
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#isRollAtMidnight()
     */
    @Override
    public boolean isRollAtMidnight ()
    {
        return _rollAtMidnight;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#setRollAtMidnight(boolean)
     */
    @Override
    public void setRollAtMidnight (boolean rollAtMidnight)
    {
        _rollAtMidnight = rollAtMidnight;
    }
    /**
     * @return Timestamp (msec.) when current file was opened - if
     * non-positive then no current file assumed to be open
     */
    public abstract long getCurrentFileOpenTime ();

    public boolean isRollByMidnightRequired ()
    {
        if (!isRollAtMidnight())    // OK if not required to roll at midnight
            return false;

        final long    openTime=getCurrentFileOpenTime();
        if (openTime <= 0L)    // OK if no currently open file
            return false;

        final Calendar    calOpen=Calendar.getInstance(), calNow=Calendar.getInstance();
        calOpen.setTimeInMillis(openTime);

        // assume any date change means midnight exceeded
        if (DateUtil.compareDates(calOpen, calNow) != 0)
            return true;    // just so we have a debug breakpoint

        return false;
    }

    private String    _curIndex="a";
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#getCurrentFileIndex()
     */
    @Override
    public String getCurrentFileIndex ()
    {
        return _curIndex;
    }

    public static final String    DEFAULT_LOG_FILE_NAME_PREFIX="log";
    private String    _namePrefix    /* =null */;
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#getFileNamePrefix()
     */
    @Override
    public String getFileNamePrefix ()
    {
        if ((null == _namePrefix) || (_namePrefix.length() <= 0))
            return DEFAULT_LOG_FILE_NAME_PREFIX;

        return _namePrefix;
    }
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#setFileNamePrefix(java.lang.String)
     */
    @Override
    public void setFileNamePrefix (String prfx)
    {
        _namePrefix = prfx;
    }

    protected String buildBaseLogFileName ()
    {
        final StringBuilder    sb=new StringBuilder(64);
        {
            final String    prefix=getFileNamePrefix();
            if ((prefix != null) && (prefix.length() > 0))
            {
                if (sb.length() > 0)
                    sb.append('-');
                sb.append(prefix);
            }
        }

        {
            final Calendar    calNow=Calendar.getInstance();
            final int[]        comps={
                    calNow.get(Calendar.YEAR),                            4,
                    calNow.get(Calendar.MONTH) - Calendar.JANUARY + 1,    2,
                    calNow.get(Calendar.DAY_OF_MONTH),                    2
                };
            for (int    cIndex=0; cIndex < comps.length; cIndex += 2)
            {
                final int    cVal=comps[cIndex], cWidth=comps[cIndex+1];
                if (sb.length() > 0)
                    sb.append('-');

                try
                {
                    StringUtil.appendPaddedNum(sb, cVal, cWidth);
                }
                catch(IOException e)
                {
                    // should not happen
                    sb.append("ERR");
                }
            }

            sb.append('-');
        }

        return sb.toString();
    }

    protected void closeLoggingStream (final PrintStream ps)
    {
        if (null == ps)    // OK if no active stream
            return;
        // check if have any footer in newly created file
        final Layout    l=getLayout();
        final String    ftr=(null == l) /* OK */ ? null : l.getFooter(),
                        end=cleanupLogText(ftr);
        if ((end != null) && (end.length() > 0))
            ps.println(end);

        ps.close();
    }
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#rollOver()
     */
    @Override
    public boolean rollOver ()
    {
        if (_ps != null)
        {
            closeLoggingStream(_ps);
            _ps = null;    // force re-open on next event appended
        }

        // check if need to re-start files index
        final File         curFile=getCurrentLogFile();
        final String    curName=(null == curFile) /* OK */ ? null : curFile.getName(),
                        baseFileName=buildBaseLogFileName();
        if ((curName != null) && (baseFileName != null))
        {
            // if date prefix changed then re-start files index
            if (!curName.startsWith(baseFileName))
                _curIndex = "a";
        }

        if (curFile != null)    // mark that no currently active log file
            _curFile = null;

        return (curFile != null);
    }

    protected static final Collection<File> getAppendCandidates (final File     logDir,
                                                                 final String    baseFileName,
                                                                 final String    fileExt,
                                                                 final long        appendDiff)
    {
        final int    bnLen=(null == baseFileName) ? 0 : baseFileName.length(),
                    extLen=(null == fileExt) ? 0 : fileExt.length();
        if ((null == logDir) || (!logDir.exists()) || (!logDir.isDirectory())
         || (bnLen <= 0)
         || (extLen <= 0)
         || (appendDiff <= 0L))
            return null;

        final FileFilter    ff=new IndexedFilesFilter(baseFileName, fileExt);
        final File[]        logFiles=logDir.listFiles(ff);
        if ((null == logFiles) || (logFiles.length <= 0))
            return null;

        Collection<File>    ac=null;
        final long            curTime=System.currentTimeMillis();
        for (final File f : logFiles)
        {
            if ((null == f)         // should not happen
             || (!f.exists())        // should not happen
             || f.isDirectory())
                continue;

            final long    lastMod=f.lastModified(),
                        modDiff=curTime - lastMod;
            if ((modDiff >= 0L) && (modDiff <= appendDiff))
            {
                if (null == ac)
                    ac = new LinkedList<File>();
                ac.add(f);
            }
        }

        return ac;
    }
    // find highest index in use
    protected static final String adjustLoggingFileIndex (final File     logDir,
                                                          final String    baseFileName,
                                                          final String    fileExt,
                                                          final String    curIndex)
    {
        final int    bnLen=(null == baseFileName) ? 0 : baseFileName.length(),
                    extLen=(null == fileExt) ? 0 : fileExt.length();
        if ((null == logDir) || (!logDir.exists()) || (!logDir.isDirectory())
         || (bnLen <= 0)
         || (extLen <= 0)
         || (null == curIndex) || (curIndex.length() <= 0))
            return curIndex;

        final FileFilter    ff=new IndexedFilesFilter(baseFileName, fileExt);
        final File[]        logFiles=logDir.listFiles(ff);
        if ((null == logFiles) || (logFiles.length <= 0))
            return curIndex;    // OK if no current files with the same base file name

        String    maxIndex=curIndex;
        for (final File f : logFiles)
        {
            if ((null == f) /* should not happen */ || f.isDirectory())
                continue;

            final String    fName=f.getName();
            final int        fnLen=(null == fName) /* should not happen */ ? 0 : fName.length();
            if ((fnLen <= 0) || (fnLen <= bnLen) || (!fName.startsWith(baseFileName)))
                continue;

            final String    extIndex;
            if (extLen > 0)
            {
                final int        fnSPos=fName.lastIndexOf('.');
                final String    fnExt=
                    ((fnSPos <= 0) || (fnSPos >= (fnLen - 1))) ? null : fName.substring(fnSPos);
                if ((null == fnExt) || (fnExt.length() <= 0) || (!fnExt.equalsIgnoreCase(fileExt)))
                    continue;

                extIndex = fName.substring(bnLen, fnSPos);
            }
            else
                extIndex = fName.substring(bnLen);

            final int    idxLen=(null == extIndex) ? 0 : extIndex.length();
            if (idxLen <= 0)
                continue;

            for (int    cIndex=0; cIndex < idxLen; cIndex++)
            {
                final char    c=extIndex.charAt(cIndex);
                // allow only lowercase 'a'-'z' characters
                if ((c < 'a') || (c > 'z'))
                    continue;
            }

            // check if have a higher index value
            if ((null == maxIndex) || (maxIndex.compareTo(extIndex) < 0))
                maxIndex = extIndex;
        }

        if ((null == maxIndex) || (maxIndex.length() <= 0))
            return curIndex;

        return maxIndex;
    }

    private long    _appendTimeDiff    /* =0L */;
    /*
     * @see net.community.chest.apache.log4j.appender.RollingFileAppenderController#getAppendTimeDiff()
     */
    @Override
    public long getAppendTimeDiff ()
    {
        return _appendTimeDiff;
    }

    @Override
    public void setAppendTimeDiff (long appendTimeDiff)
    {
        _appendTimeDiff = appendTimeDiff;
    }
    /**
     * @param curIndex current rolling file index - may be null/empty
     * @return next rolling file index
     */
    public static final String getNextIndex (final String curIndex)
    {
        final int    iLen=(null == curIndex) ? 0 : curIndex.length();
        if (iLen <= 0)
            return "a";

        char        ciChar=curIndex.charAt(iLen-1);
        // check if exhausted all letters 'a'-'z'
        if (ciChar < 'z')
        {
            ciChar++;

            if (iLen > 1)
            {
                final String    iPrefix=curIndex.substring(0, iLen - 1);
                return iPrefix + String.valueOf(ciChar);
            }
            else    // shortcut for single character
                return String.valueOf(ciChar);
        }
        else    // start from 'a' but with a longer index
            return (curIndex + "a");
    }
    /**
     * Chooses a {@link File} to be used for appending to from the given
     * "candidates" {@link Collection} using the one most recently modified
     * @param <F> The {@link File} generic type
     * @param ac The "candidate" {@link File}-s {@link Collection}
     * @return most recently modified member - null if no candidates
     */
    public static final <F extends File> F chooseAppendFile (final Collection<? extends F> ac)
    {
        final int        numFiles=(null == ac) /* OK if no candidates */ ? 0 : ac.size();
        final File[]    fa=(numFiles <= 0) ? null : ac.toArray(new File[numFiles]);
        if ((null == fa) || (fa.length <= 0))
            return null;    // OK if no candidates

        // this sorts the array from oldest to most recent, so we have to scan the array "backwards"
        if (fa.length > 1)
            Arrays.sort(fa, LastModifiedTimeComparator.ASCENDING);
        for (int    fIndex=fa.length-1; fIndex >= 0; fIndex--)
        {
            final File    f=fa[fIndex];
            if (f != null)    // should not be otherwise
            {
                @SuppressWarnings("unchecked")
                final F    ff=(F) f;
                return ff;
            }
        }

        return null;
    }
    /**
     * Called whenever a new logging file is required
     * @return The {@link File} to use for logging - <B>Note:</B> should
     * not change while {@link #rollOver()} has not been called (may NOT
     * be null)
     */
    protected File assignLoggingFile ()
    {
        // check if need to re-start files index
        {
            final long    lastOpenTime=getCurrentFileOpenTime();
            if (lastOpenTime > 0L)
            {
                final Calendar    opnTime=Calendar.getInstance(), nowTime=Calendar.getInstance();
                opnTime.setTimeInMillis(lastOpenTime);

                // re-start files index if not same date
                if (DateUtil.compareDates(opnTime, nowTime) != 0)
                    _curIndex = "a";
            }
        }

        final String                        baseFileName=buildBaseLogFileName(),
                                            fileExt=getExtension();
        final File                            logDir=getLogDir();
        final long                            appendDiff=getAppendTimeDiff();
        final Collection<? extends File>    appendCandidates=
            getAppendCandidates(logDir, baseFileName, fileExt, appendDiff);

        _curIndex = adjustLoggingFileIndex(logDir, baseFileName, fileExt, _curIndex);

        // we "limit" ourselves to ~32767 retries
        for (int    tIndex=0; tIndex < Short.MAX_VALUE; tIndex++, _curIndex = getNextIndex(_curIndex))
        {
            final String    chkName=baseFileName + _curIndex + fileExt;
            final File        chkFile=new File(logDir, chkName);
            // avoid overwriting an existing file
            if (!chkFile.exists())
            {
                // if append difference feature enabled and have some candidates, then return the most recent one
                if (appendDiff > 0L)
                {
                    final File    apndFile=chooseAppendFile(appendCandidates);
                    if (apndFile != null)    // can be null if no candidates
                        return apndFile;
                }

                return chkFile;
            }
        }

        // unexpected / should not happen
        errorReport("assignLoggingFile() all files exhausted");
        return null;
    }

    private File    _curFile    /* =null */;
    /*
     * @see net.community.chest.apache.log4j.appender.AbstractFileAppender#getCurrentFilePath()
     */
    @Override
    public File getCurrentLogFile ()
    {
        return _curFile;
    }

    private PrintStream    _ps    /* =null */;
    /*
     * @see java.io.Flushable#flush()
     */
    @Override
    public void flush () throws IOException
    {
        if (_ps != null)
            _ps.flush();
    }

    protected PrintStream getLoggingPrintStream ()
    {
        if (null == _ps)
        {
            FileOutputStream    fout=null;
            try
            {
                if (null == (_curFile=assignLoggingFile()))
                    throw new IllegalStateException("No logging file assigned");

                final File        logDir=_curFile.getParentFile();
                final boolean    okDir;
                if (logDir != null)
                {
                    if (logDir.exists())
                        okDir = logDir.isDirectory();
                    else
                        okDir = logDir.mkdirs();
                }
                else
                    okDir = false;
                if (!okDir)
                    throw new IOException("Failed to ensure existance of directory(ies) for " + _curFile);

                boolean    useAppendMode=false;
                // if assigned an existing file, then check if the append feature is enabled
                if (_curFile.exists())
                {
                    final long    apndDiff=getAppendTimeDiff(),
                                lastMod=_curFile.lastModified(),
                                curTime=System.currentTimeMillis();
                    // if feature enabled we re-check it - even though "assignLoggingFile" took care of it
                    if (apndDiff > 0L)
                    {
                        final long    modDiff=curTime - lastMod;
                        if ((modDiff >= 0L) && (modDiff <= apndDiff))
                            useAppendMode = true;
                    }
                }

                fout = new FileOutputStream(_curFile, useAppendMode);

                final int    bufSize=getIOBufSizeKB() * 1024;
                if (bufSize > 0)
                    _ps = new PrintStream(new BufferedOutputStream(fout, bufSize));
                else
                    _ps = new PrintStream(fout);

                if (!useAppendMode)
                {
                    // check if have any header in newly created file
                    final Layout    l=getLayout();
                    final String    hdr=(null == l) /* OK */ ? null : l.getHeader(),
                                    ttl=cleanupLogText(hdr);
                    if ((ttl != null) && (ttl.length() > 0))
                        _ps.println(ttl);
                }

                fout = null;    // disable auto-close on 'finally' clause
            }
            catch(Exception e)
            {
                errorReport("getLoggingPrintStream()", e, (-1));
            }
            finally
            {
                if (fout != null)
                {
                    try
                    {
                        fout.close();
                    }
                    catch(IOException e)
                    {
                        // ignored
                    }
                    fout = null;
                }
            }
        }

        return _ps;
    }
    /*
     * @see net.community.chest.apache.log4j.AbstractAppender#appendFormattedEvent(org.apache.log4j.spi.LoggingEvent, java.lang.String)
     */
    @Override
    protected boolean appendFormattedEvent (LoggingEvent e, String msg)
    {
        if ((null == msg) || (msg.length() <= 0))
            return false;

        final PrintStream    ps=getLoggingPrintStream();
        if (null == ps)
            return false;
        ps.println(msg);

        // flush contents if WARN/ERROR/FATAL
        final Level    lvl=(null == e) ? null : e.getLevel();
        if ((lvl != null) && lvl.isGreaterOrEqual(Level.WARN))
            ps.flush();

        if (isRollByMidnightRequired() || isRollBySizeRequired())
        {
            closeLoggingStream(ps);

            rollOver();
            removeOldFiles();

            // we close the stream here in case the "rollOver/removeOldFiles" overrides want to write some closure information
            if (_ps != null)
            {
                _ps.close();
                _ps = null;    // force re-open on next event appended
            }
        }

        return true;
    }
    /*
     * @see net.community.chest.apache.log4j.AbstractAppender#close()
     */
    @Override
    public void close ()
    {
        rollOver();
        removeOldFiles();

        // just making sure again since "rollOver" should have taken care of that
        if (_ps != null)
        {
            _ps.close();
            _ps = null;
        }

        super.close();
    }
}
