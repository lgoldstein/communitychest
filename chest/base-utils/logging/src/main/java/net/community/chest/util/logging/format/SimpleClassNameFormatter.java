package net.community.chest.util.logging.format;

import net.community.chest.util.logging.LogLevelWrapper;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 26, 2007 2:12:19 PM
 */
public class SimpleClassNameFormatter extends LogMsgComponentFormatter<Class<?>> {
    public SimpleClassNameFormatter ()
    {
        super(SIMPLE_CLASS_NAME);
    }
    /*
     * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#formatValue(java.lang.Object)
     */
    @Override
    public String formatValue (Class<?> value)
    {
        return (null == value) ? null : value.getSimpleName();
    }
    /*
     * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#format(java.lang.Thread, long, java.lang.Class, java.util.logging.Level, java.lang.Object, java.lang.String, java.lang.Throwable)
     */
    @Override
    public String format (Thread th, long logTime, Class<?> logClass, LogLevelWrapper l, Object ctx, String msg, Throwable t)
    {
        return formatValue(logClass);
    }
}
