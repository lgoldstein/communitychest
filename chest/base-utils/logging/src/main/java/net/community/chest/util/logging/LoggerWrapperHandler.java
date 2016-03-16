/*
 *
 */
package net.community.chest.util.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 24, 2010 3:07:06 PM
 */
public class LoggerWrapperHandler extends Handler {
    public LoggerWrapperHandler ()
    {
        super();
    }

    private boolean    _open=true;
    /*
     * @see java.util.logging.Handler#close()
     */
    @Override
    public void close () throws SecurityException
    {
        if (_open)
            _open = false;
    }
    /*
     * @see java.util.logging.Handler#flush()
     */
    @Override
    public void flush ()
    {
        if (!_open)
            throw new IllegalStateException("flush() handler is closed");
    }

    public static final LogLevelWrapper xlateLevel (Level lvl)
    {
        if (null == lvl)
            return null;

        if (Level.SEVERE.equals(lvl))
            return LogLevelWrapper.ERROR;
        else if (Level.WARNING.equals(lvl))
            return LogLevelWrapper.WARNING;
        else if (Level.INFO.equals(lvl))
            return LogLevelWrapper.INFO;
        else if (Level.FINE.equals(lvl))
            return LogLevelWrapper.DEBUG;
        else if (Level.FINER.equals(lvl))
            return LogLevelWrapper.VERBOSE;
        else if (Level.FINEST.equals(lvl))
            return LogLevelWrapper.TRACE;
        else
            return LogLevelWrapper.DEBUG;
    }
    /*
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    @Override
    public void publish (LogRecord record)
    {
        if ((!_open) || (null == record) || (!isLoggable(record)))
            return;

        final LogLevelWrapper    level=xlateLevel(record.getLevel());
        final LoggerWrapper        logger=
            WrapperFactoryManager.getLogger(getClass(), record.getLoggerName(), null);
        if ((null == level) || (null == logger) || (!logger.isEnabledFor(level)))
            return;

        logger.log(level, record.getMessage(), record.getThrown());
    }
}
