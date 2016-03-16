package net.community.chest.util.logging.format;

import net.community.chest.util.logging.LogLevelWrapper;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 27, 2007 1:30:44 PM
 */
public class MessageTextFormatter extends LogMsgComponentFormatter<String> {
    public MessageTextFormatter ()
    {
        super(MESSAGE);
    }
    /*
     * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#formatValue(java.lang.Object)
     */
    @Override
    public String formatValue (final String value)
    {
        final String    tv=
            ((null == value) || (value.length() <= 0)) ? null : value.trim(),
                        cv=
            ((null == tv) || (tv.length() <= 0)) ? null : tv.replace('\r', ' '),
                        lv=
            ((null == cv) || (cv.length() <= 0)) ? null : cv.replace('\n', ' ');
        return lv;
    }
    /*
     * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#format(java.lang.Thread, long, java.lang.Class, java.util.logging.Level, java.lang.Object, java.lang.String, java.lang.Throwable)
     */
    @Override
    public String format (Thread th, long logTime, Class<?> logClass, LogLevelWrapper l, Object ctx, String msg, Throwable t)
    {
        return formatValue(msg);
    }
}
