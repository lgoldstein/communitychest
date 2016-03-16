/*
 *
 */
package net.community.chest.apache.log4j.appender;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Oct 28, 2010 3:23:07 PM
 */
public class Log4jToJulAppender extends AppenderSkeleton {
    public Log4jToJulAppender ()
    {
        super();
    }

    public Log4jToJulAppender (boolean isActive)
    {
        super(isActive);
    }
    /*
     * @see org.apache.log4j.Appender#close()
     */
    @Override
    public void close ()
    {
        // ignored
    }
    /*
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    @Override
    public boolean requiresLayout ()
    {
        return false;
    }

    public static final java.util.logging.Level xlateLevel (final org.apache.log4j.Level level)
    {
        if (level == null)
            return null;

        final int    lVal=level.toInt();
        switch(lVal)
        {
            case org.apache.log4j.Priority.ALL_INT        :
                return java.util.logging.Level.ALL;
            case org.apache.log4j.Priority.OFF_INT        :
                return java.util.logging.Level.OFF;
            case org.apache.log4j.Priority.FATAL_INT    :
            case org.apache.log4j.Priority.ERROR_INT    :
                return java.util.logging.Level.SEVERE;
            case org.apache.log4j.Priority.WARN_INT    :
                return java.util.logging.Level.WARNING;
            case org.apache.log4j.Priority.INFO_INT    :
                return java.util.logging.Level.INFO;
            case org.apache.log4j.Priority.DEBUG_INT    :
                return java.util.logging.Level.FINE;
            case org.apache.log4j.Level.TRACE_INT        :
                return java.util.logging.Level.FINER;
            default                                        :
                return java.util.logging.Level.FINEST;
        }
    }

    public static final LogRecord xlateLoggingEvent (LoggingEvent event)
    {
        final org.apache.log4j.Level    eLevel=(event == null) ? null : event.getLevel();
        final java.util.logging.Level    level=xlateLevel(eLevel);
        if (level == null)
            return null;

        final LogRecord    logRec=new LogRecord(level, String.valueOf(event.getMessage()));
        logRec.setLoggerName(event.getLoggerName());
        logRec.setSourceClassName(event.getLoggerName());
        logRec.setMillis(event.getTimeStamp());
        return logRec;
    }
    /*
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected void append (LoggingEvent event)
    {
        final org.apache.log4j.Level    eLevel=(event == null) ? null : event.getLevel();
        if (!isAsSevereAsThreshold(eLevel))
            return;

        final Logger                    logger=(eLevel == null) ? null : Logger.getLogger(event.getLoggerName());
        final java.util.logging.Level    jLevel=xlateLevel(eLevel);
        if ((jLevel == null) || (logger == null) || (!logger.isLoggable(jLevel)))
            return;

        final LogRecord    logRec=xlateLoggingEvent(event);
        if (logRec == null)
            return;

        logger.log(logRec);
    }
}
