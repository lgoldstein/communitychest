package net.community.chest.util.logging.format;

import net.community.chest.io.EOLStyle;
import net.community.chest.util.logging.LogLevelWrapper;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 26, 2007 2:25:11 PM
 */
public class StackTraceFormatter extends LogMsgComponentFormatter<Throwable> {
    /**
     * Max. depth of stack trace elements to show. If zero, then nothing
     * is shown. If negative - then same as "all"
     */
    private int    _maxDepth    /* =0 */;
    public int getMaxDepth ()
    {
        return _maxDepth;
    }

    public void setMaxDepth (int maxDepth)
    {
        _maxDepth = maxDepth;
    }

    public StackTraceFormatter (final int maxDepth)
    {
        super(STACKTRACE);

        _maxDepth = maxDepth;
    }

    /**
     * Default displayed stack trace depth if none specified via
     * {@link #STACK_TRACE_DEPTH_PROP_NAME} property
     */
    public static final int DEFAULT_DEPTH=5;
    /**
     * @param depth stack trace depth - if null/empty/bad-format then
     * {@link #DEFAULT_DEPTH} is used.
     */
    public StackTraceFormatter (final String depth)
    {
        super(STACKTRACE);

        if ((depth != null) && (depth.length() > 0))
        {
            try
            {
                _maxDepth = Integer.parseInt(depth);
                return;
            }
            catch(Exception e)
            {
                // ignored
            }
        }

        // this point is reached if max. depth not specified or bad format
        _maxDepth = DEFAULT_DEPTH;
    }
    /**
     * Property that can be used to define the default stack trace depth to be
     * used. If missing/bad-format then {@link #DEFAULT_DEPTH} is used.
     * @see #_maxDepth
     */
    public static final String    STACK_TRACE_DEPTH_PROP_NAME=StackTraceFormatter.class.getName().toLowerCase() + ".depth";
    /**
     * Default (empty) constructor - uses {@link #STACK_TRACE_DEPTH_PROP_NAME}
     * property to initialize the max. depth value
     */
    public StackTraceFormatter ()
    {
        this(System.getProperty(STACK_TRACE_DEPTH_PROP_NAME, String.valueOf(DEFAULT_DEPTH)));
    }
    /*
     * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#formatValue(java.lang.Object)
     */
    @Override
    public String formatValue (Throwable value)
    {
        final int    maxDepth=getMaxDepth();
        if ((0 == maxDepth) /* OK if no depth set */ || (null == value) /* OK if no exception */)
            return "";

        final StackTraceElement[]    elems=value.getStackTrace();
        final int                    numElems=(null == elems) /* should not happen */ ? 0 : elems.length;
        if (numElems <= 0)    // should not happen
            return null;    // just to signal that something should be there

        final int            effElems=
            (maxDepth > 0) ? Math.min(maxDepth, numElems) : numElems;
        final StringBuilder    sb=
            (effElems <= 0) ? null : new StringBuilder(effElems * 128);
        for (int    eIndex=0; eIndex < effElems; eIndex++)
        {
            final StackTraceElement    e=elems[eIndex];
            final String            eString=(null == e) /* should not happen */ ? null : e.toString();
            if ((null == eString) || (eString.length() <= 0))
                continue;    // should not happen

            if (sb.length() > 0)
                sb.append(EOLStyle.LOCAL.getStyleChars());
            sb.append(eString);
        }

        return ((null == sb) || (sb.length() <= 0)) ? "" : sb.toString();
    }
    /*
     * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#format(java.lang.Thread, long, java.lang.Class, java.util.logging.Level, java.lang.Object, java.lang.String, java.lang.Throwable)
     */
    @Override
    public String format (Thread th, long logTime, Class<?> logClass, LogLevelWrapper l, Object ctx, String msg, Throwable t)
    {
        return formatValue(t);
    }
}
