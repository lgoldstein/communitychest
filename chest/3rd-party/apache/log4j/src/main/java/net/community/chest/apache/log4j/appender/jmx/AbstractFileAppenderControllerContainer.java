package net.community.chest.apache.log4j.appender.jmx;

import java.io.IOException;
import java.io.StreamCorruptedException;


/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Reflects all {@link AbstractFileAppenderController} interface methods using a
 * contained instance supplied via an <code>abstract</code> method</P>
 *
 * @author Lyor G.
 * @since Oct 2, 2007 11:10:35 AM
 */
public abstract class AbstractFileAppenderControllerContainer implements AbstractFileAppenderController {
    protected AbstractFileAppenderControllerContainer ()
    {
        super();
    }

    protected abstract AbstractFileAppenderController getFileAppenderControllerInstance ();
    /*
     * @see java.io.Flushable#flush()
     */
    @Override
    public void flush () throws IOException
    {
        final AbstractFileAppenderController    inst=getFileAppenderControllerInstance();
        if (null == inst)
            throw new StreamCorruptedException("No current " + AbstractFileAppenderController.class.getName() + " instance");

        inst.flush();
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#getCurrentFileSize()
     */
    @Override
    public long getCurrentFileSize ()
    {
        final AbstractFileAppenderController    inst=getFileAppenderControllerInstance();
        return (null == inst) ? 0L : inst.getCurrentFileSize();
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#getCurrentLogFilePath()
     */
    @Override
    public String getCurrentLogFilePath ()
    {
        final AbstractFileAppenderController    inst=getFileAppenderControllerInstance();
        return (null == inst) ? null : inst.getCurrentLogFilePath();
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#getExtension()
     */
    @Override
    public String getExtension ()
    {
        final AbstractFileAppenderController    inst=getFileAppenderControllerInstance();
        return (null == inst) ? null : inst.getExtension();
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#setExtension(java.lang.String)
     */
    @Override
    public void setExtension (String extension)
    {
        final AbstractFileAppenderController    inst=getFileAppenderControllerInstance();
        inst.setExtension(extension);    // cause intentional NullPointerException
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#getIOBufSizeKB()
     */
    @Override
    public int getIOBufSizeKB ()
    {
        final AbstractFileAppenderController    inst=getFileAppenderControllerInstance();
        return (null == inst) ? 0 : inst.getIOBufSizeKB();
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#setIOBufSizeKB(int)
     */
    @Override
    public void setIOBufSizeKB (int bufSize)
    {
        final AbstractFileAppenderController    inst=getFileAppenderControllerInstance();
        inst.setIOBufSizeKB(bufSize);    // cause intentional NullPointerException
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#getLogPath()
     */
    @Override
    public String getLogPath ()
    {
        final AbstractFileAppenderController    inst=getFileAppenderControllerInstance();
        return (null == inst) ? null : inst.getLogPath();
    }
    /*
     * @see net.community.chest.apache.log4j.appender.FileAppenderController#setLogPath(java.lang.String)
     */
    @Override
    public void setLogPath (String logPath)
    {
        final AbstractFileAppenderController    inst=getFileAppenderControllerInstance();
        inst.setLogPath(logPath);    // cause intentional NullPointerException
    }
}
