package net.community.chest.util.logging.format;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.community.chest.util.logging.LogLevelWrapper;

/**
 * Copyright 2007 as per GPLv2
 *
 * Formats the timestamp of the log message according to the supplied
 * @author Lyor G.
 * @since Jun 27, 2007 1:31:56 PM
 */
public class TimestampFormatter extends LogMsgComponentFormatter<Calendar> {
    private DateFormat    _fmt;
    public DateFormat getFormat ()
    {
        return _fmt;
    }

    public void setFormat (DateFormat fmt)
    {
        _fmt = fmt;
    }
    /**
     * @param fmt {@link DateFormat} to use - if null then {@link DateFormat#getDateTimeInstance()}
     * result is used
     */
    public TimestampFormatter (final DateFormat fmt)
    {
        super(TIMESTAMP);

        if (null == (_fmt=fmt))
            _fmt = DateFormat.getDateTimeInstance();
    }
    /**
     * @param fmt a {@link SimpleDateFormat} string - if null/empty then same
     * as calling the default (empty) constructor
     * @see #TimestampFormatter()
     */
    public TimestampFormatter (final String fmt)
    {
        this(((null == fmt) || (fmt.length() <= 0)) ? null : new SimpleDateFormat(fmt));
    }
    /**
     * Default (empty) constructor - initialize the formatting to {@link DateFormat#getDateTimeInstance()}
     * @see #TimestampFormatter(DateFormat)
     */
    public TimestampFormatter ()
    {
        this((DateFormat) null);
    }

    public String formatValue (final Date dtv)
    {
        final DateFormat    fmt=getFormat();
        if ((null == fmt) || (null == dtv))
            return null;

        synchronized(fmt)
        {
            return fmt.format(dtv);
        }
    }

    public String formatValue (final long msec)
    {
        return formatValue(new Date(msec));
    }
    /*
     * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#formatValue(java.lang.Object)
     */
    @Override
    public String formatValue (final Calendar value)
    {
        return (null == value) ? null : formatValue(value.getTime());
    }
    /*
     * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#format(java.lang.Thread, long, java.lang.Class, java.util.logging.Level, java.lang.Object, java.lang.String, java.lang.Throwable)
     */
    @Override
    public String format (Thread th, long logTime, Class<?> logClass, LogLevelWrapper l, Object ctx, String msg, Throwable t)
    {
        return formatValue(logTime);
    }
}
