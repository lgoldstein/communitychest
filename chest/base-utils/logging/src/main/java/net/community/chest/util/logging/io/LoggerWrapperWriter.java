/*
 *
 */
package net.community.chest.util.logging.io;

import java.io.IOException;
import net.community.chest.io.output.LineWriter;
import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.LoggerWrapper;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Implements a {@link java.io.Writer} over a {@link LoggerWrapper} where written data
 * is separated into messages using the <code>line.separator</code></P>
 * @author Lyor G.
 * @since May 18, 2009 10:14:23 AM
 */
public class LoggerWrapperWriter extends LineWriter {
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
     * @see net.community.chest.io.output.LineLevelAppender#isWriteEnabled()
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

    public LoggerWrapperWriter (LoggerWrapper logger, LogLevelWrapper lvl)
    {
        _logger = logger;
        _outputLevel = lvl;
    }

    public LoggerWrapperWriter ()
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
