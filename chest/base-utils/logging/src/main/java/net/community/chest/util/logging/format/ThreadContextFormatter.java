package net.community.chest.util.logging.format;

import net.community.chest.util.logging.LogLevelWrapper;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 26, 2007 2:15:55 PM
 */
public class ThreadContextFormatter extends LogMsgComponentFormatter<Object> {
    public ThreadContextFormatter ()
    {
        super(CONTEXT_DATA);
    }
    /*
     * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#formatValue(java.lang.Object)
     */
    @Override
    public String formatValue (Object value)
    {
        return (null == value) ? "" : value.toString();
    }
    /*
     * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#format(java.lang.Thread, long, java.lang.Class, java.util.logging.Level, java.lang.Object, java.lang.String, java.lang.Throwable)
     */
    @Override
    public String format (Thread th, long logTime, Class<?> logClass, LogLevelWrapper l, Object ctx, String msg, Throwable t)
    {
        return formatValue(ctx);
    }
}
