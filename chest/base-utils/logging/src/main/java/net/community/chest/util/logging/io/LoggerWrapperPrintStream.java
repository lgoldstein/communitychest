/*
 *
 */
package net.community.chest.util.logging.io;

import java.io.IOException;
import net.community.chest.io.output.LinePrintStream;
import net.community.chest.io.output.NullOutputStream;
import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.LoggerWrapper;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Implements {@link java.io.PrintStream} functionality over {@link LoggerWrapper}
 * messages where each line is a separate message</P>
 *
 * @author Lyor G.
 * @since May 21, 2009 12:50:42 PM
 */
public class LoggerWrapperPrintStream extends LinePrintStream {
    /**
     * The {@link LoggerWrapper} instance used to execute the logging
     */
    private LoggerWrapper    _logger;
    public LoggerWrapper getLogger ()
    {
        return _logger;
    }

    public void setLogger (LoggerWrapper logger)
    {
        _logger = logger;
    }
    /**
     * The log level of the output message(s)
     */
    private LogLevelWrapper    _outputLevel;
    public LogLevelWrapper getOutputLevel ()
    {
        return _outputLevel;
    }

    public void setOutputLevel (LogLevelWrapper outputLevel)
    {
        _outputLevel = outputLevel;
    }
    /*
     * @return TRUE if have a valid {@link LoggerWrapper} and
     * {@link LogLevelWrapper} instance and {@link LoggerWrapper#isEnabledFor(LogLevelWrapper)}
     * returns <code>true</code>
     * @see net.community.chest.io.output.LinePrintStream#isWriteEnabled()
     */
    @Override
    public boolean isWriteEnabled ()
    {
        final LoggerWrapper    logger=getLogger();
        if (null == logger)
            return false;

        final LogLevelWrapper    lvl=getOutputLevel();
        if (null == lvl)
            return false;

        if (!logger.isEnabledFor(lvl))
            return false;    // debug breakpoint

        return true;
    }

    public LoggerWrapperPrintStream (LoggerWrapper logger, LogLevelWrapper lvl)
    {
        super(new NullOutputStream());
        _logger = logger;
        _outputLevel = lvl;
    }

    public LoggerWrapperPrintStream ()
    {
        this(null, null);
    }
    /*
     * @see net.community.chest.io.output.LineLevelAppender#writeLineData(java.lang.StringBuilder, int)
     */
    @Override
    public void writeLineData (final StringBuilder sb, final int dLen) throws IOException
    {
        // this point is reached if data buffer contains line separator pattern
        final LoggerWrapper        logger=getLogger();
        final LogLevelWrapper    lvl=getOutputLevel();
        if ((dLen > 0) /* ignore empty lines */ && (logger != null) && (lvl != null))
        {
            final String    msg=sb.substring(0, dLen /* without the line separator */);
            logger.log(lvl, msg);
        }

        if (sb.length() > 0)
            sb.setLength(0);
    }
}
