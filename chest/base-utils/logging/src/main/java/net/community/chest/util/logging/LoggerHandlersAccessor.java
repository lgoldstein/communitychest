/*
 *
 */
package net.community.chest.util.logging;

import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 24, 2010 2:58:21 PM
 */
public final class LoggerHandlersAccessor {
    private LoggerHandlersAccessor ()
    {
        // disable instance
    }
    /**
     * Replaces all handlers of specified logger with new ones
     * @param l logger whose handlers are to be replaced - may NOT be null
     * @param replaceInherited if TRUE then handlers are replaced even if
     * inherited. Otherwise, if no handlers, then nothing done. <B>Note:</B>
     * unless you <U>really</U> know what you are doing, pass FALSE value
     * @param h new handlers to use - may NOT be null/empty, but may contain
     * null elements - provided <U>at least <B>one</B></U> non-null element
     * is found (otherwise exception is thrown - see below)
     * @return replaced (original) handlers - may be null
     * @throws IllegalArgumentException if null/empty handlers array
     * @throws IllegalStateException if after replacement no handlers left. This
     * may occur if the array was not null but contained only null object
     */
    public static final Handler[] replaceHandlers (Logger l, boolean replaceInherited, Handler... h)
    {
        final int    newNum=(null == h) ? 0 : h.length;
        if (newNum <= 0)
            throw new IllegalArgumentException("replaceHandlers(" + l.getName() + ") no handlers[] to replace");

        final Handler[]    prev=l.getHandlers();
        final int        numPrev=(null == prev) ? 0 : prev.length;
        if ((numPrev <= 0) && (!replaceInherited))
            return prev;

        for (int    hIndex=0; hIndex < numPrev; hIndex++)
        {
            final Handler    hPrev=prev[hIndex];
            if (null == hPrev)    // should not happen
                continue;
            l.removeHandler(hPrev);
        }

        for (int    hIndex=0; hIndex < newNum; hIndex++)
        {
            final Handler    hNew=h[hIndex];
            if (null == hNew)    // should not happen
                continue;
            l.addHandler(hNew);
        }

        final Handler[]    cur=l.getHandlers();
        if ((null == cur) || (cur.length <= 0))
            throw new IllegalStateException("replaceHandlers(" + l.getName() + " no handlers left after replacing");

        return prev;
    }
    /**
     * Replaces all handlers of specified logger with new ones if <U>not inherited</U>
     * @param l logger whose handlers are to be replaced - may NOT be null
     * @param h new handlers to use - may NOT be null/empty, but may contain
     * null elements - provided <U>at least <B>one</B></U> non-null element
     * is found (otherwise exception is thrown - see below)
     * @return replaced (original) handlers - may be null
     * @see #replaceHandlers(Logger, boolean, Handler...)
     */
    public static final Handler[] replaceHandlers (Logger l, Handler... h)
    {
        return replaceHandlers(l, false, h);
    }
    /**
     * Replaces all handlers of specified logger with new one
     * @param l logger whose handlers are to be replaced - may NOT be null
     * @param replaceInherited if TRUE then handlers are replaced even if
     * inherited. Otherwise, if no handlers, then nothing done. <B>Note:</B>
     * unless you <U>really</U> know what you are doing, pass FALSE value
     * @param h new handler to use - may NOT be null/empty
     * @return replaced (original) handlers - may be null
     * @see #replaceHandlers(Logger, boolean, Handler...)
     */
    public static final Handler[] replaceHandler (Logger l, boolean replaceInherited, Handler h)
    {
        return replaceHandlers(l, replaceInherited, h);
    }
    /**
     * Replaces all handlers of specified logger with new ones if <U>not inherited</U>
     * Replaces all handlers of specified logger with new one
     * @param l logger whose handlers are to be replaced - may NOT be null
     * @param h new handler to use - may NOT be null/empty
     * @return replaced (original) handlers - may be null
     * @see #replaceHandlers(Logger, boolean, Handler...)
     */
    public static final Handler[] replaceHandler (Logger l, Handler h)
    {
        return replaceHandler(l, false, h);
    }
    /**
     * Replaces the handlers of all loggers whose name equals or start with
     * specified root name
     * @param rootName prefix of loggers names to be replaced - if null/empty,
     * then <U>all</U> loggers' handlers will be replaced.
     * @param replaceInherited if TRUE then handlers are replaced even if
     * inherited. Otherwise, if no handlers, then nothing done. <B>Note:</B>
     * unless you <U>really</U> know what you are doing, pass FALSE value
     * @param h new handlers to use - may NOT be null/empty, but may contain
     * null elements - provided <U>at least <B>one</B></U> non-null element
     * is found (otherwise exception is thrown - see below)
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),
     * value=previous handlers(array of {@link Handler}-s). If no previous handlers were replaced, then
     * logger is not mapped. <B>Note:</B> may be null if nothing replaced.
     * @see #replaceHandlers(Logger, boolean, Handler...)
     */
    public static Map<String,Handler[]> replaceRecursiveHandlers (String rootName, boolean replaceInherited, Handler... h)
    {
        Map<String,Handler[]>        m=null;
        final boolean                usePrefix=(rootName != null) && (rootName.length() > 0);
        final LogManager            lm=LogManager.getLogManager();
        final Enumeration<String>    eLoggers=(null == lm) ? null : lm.getLoggerNames();
        while ((eLoggers != null) && eLoggers.hasMoreElements())
        {
            final String    lName=eLoggers.nextElement();
            if (usePrefix && (!lName.startsWith(rootName)))
                continue;

            final Handler[]    prev=replaceHandlers(Logger.getLogger(lName), replaceInherited, h);
            if ((null == prev) || (prev.length <= 0))
                continue;    // skip if nothing replaced

            if (null == m)
                m = new TreeMap<String,Handler[]>();
            m.put(lName, prev);
        }

        return m;
    }
    /**
     * Replaces the handlers of all loggers whose name equals or start with
     * specified root name - but only if <U>not inherited</U> handlers
     * @param rootName prefix of loggers names to be replaced - if null/empty,
     * then <U>all</U> loggers' handlers will be replaced.
     * @param h new handlers to use - may NOT be null/empty, but may contain
     * null elements - provided <U>at least <B>one</B></U> non-null element
     * is found (otherwise exception is thrown - see below)
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),
     * value=previous handlers(array of {@link Handler}-s). If no previous handlers were replaced, then
     * logger is not mapped. <B>Note:</B> may be null if nothing replaced.
     * @see #replaceRecursiveHandlers(Logger, boolean, Handler ...)
     */
    public static Map<String,Handler[]> replaceRecursiveHandlers (String rootName, Handler... h)
    {
        return replaceRecursiveHandlers(rootName, false, h);
    }
    /**
     * Replaces the handlers of all loggers whose name equals or start with
     * specified root name
     * @param l root logger from which to start replacing (inclusive) - may NOT be null
     * @param replaceInherited if TRUE then handlers are replaced even if
     * inherited. Otherwise, if no handlers, then nothing done. <B>Note:</B>
     * unless you <U>really</U> know what you are doing, pass FALSE value
     * @param h new handlers to use - may NOT be null/empty, but may contain
     * null elements - provided <U>at least <B>one</B></U> non-null element
     * is found (otherwise exception is thrown - see below)
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),value=previous handlers(array of {@link Handler}-s)
     * @see #replaceRecursiveHandlers(String, boolean, Handler...)
     */
    public static Map<String,Handler[]> replaceRecursiveHandlers (Logger l, boolean replaceInherited, Handler... h)
    {
        return replaceRecursiveHandlers(l.getName(), replaceInherited, h);
    }
    /**
     * Replaces the handlers of all loggers whose name equals or start with
     * specified root name - but only if <U>not inherited</U> handlers
     * @param l root logger from which to start replacing (inclusive) - may NOT be null
     * @param h new handlers to use - may NOT be null/empty, but may contain
     * null elements - provided <U>at least <B>one</B></U> non-null element
     * is found (otherwise exception is thrown - see below)
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),value=previous handlers(array of {@link Handler}-s)
     * @see #replaceRecursiveHandlers(Logger, boolean, Handler...)
     */
    public static Map<String,Handler[]> replaceRecursiveHandlers (Logger l, Handler ... h)
    {
        return replaceRecursiveHandlers(l, false, h);
    }
    /**
     * Replaces the handlers of all loggers whose name equals or start with
     * specified root name
     * @param rootName prefix of loggers names to be replaced - if null/empty,
     * then <U>all</U> loggers' handlers will be replaced.
     * @param replaceInherited if TRUE then handlers are replaced even if
     * inherited. Otherwise, if no handlers, then nothing done. <B>Note:</B>
     * unless you <U>really</U> know what you are doing, pass FALSE value
     * @param h new handler to use
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),
     * value=previous handlers(array of {@link Handler}-s). If no previous handlers were replaced, then
     * logger is not mapped. <B>Note:</B> may be null if nothing replaced.
     * @see #replaceRecursiveHandlers(String, boolean, Handler...)
     */
    public static Map<String,Handler[]> replaceRecursiveHandler (String rootName, boolean replaceInherited, Handler h)
    {
        return replaceRecursiveHandlers(rootName, replaceInherited, h);
    }
    /**
     * Replaces the handlers of all loggers whose name equals or start with
     * specified root name
     * @param l root logger from which to start replacing (inclusive) - may NOT be null
     * @param replaceInherited if TRUE then handlers are replaced even if
     * inherited. Otherwise, if no handlers, then nothing done. <B>Note:</B>
     * unless you <U>really</U> know what you are doing, pass FALSE value
     * @param h new handler to use
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),value=previous handlers(array of {@link Handler}-s)
     * @see #replaceRecursiveHandler(String, boolean, Handler)
     */
    public static Map<String,Handler[]> replaceRecursiveHandler (Logger l, boolean replaceInherited, Handler h)
    {
        return replaceRecursiveHandler(l.getName(), replaceInherited, h);
    }
    /**
     * Replaces the handlers of all loggers whose name equals or start with
     * specified root name - but only if <U>not inherited</U> handlers
     * @param rootName prefix of loggers names to be replaced - if null/empty,
     * then <U>all</U> loggers' handlers will be replaced.
     * @param h new handler to use - may NOT be null/empty
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),value=previous handlers(array of {@link Handler}-s)
     */
    public static Map<String,Handler[]> replaceRecursiveHandler (String rootName, Handler h)
    {
        return replaceRecursiveHandler(rootName, false, h);
    }
    /**
     * Replaces the handlers of all loggers whose name equals or start with
     * specified root name - but only if <U>not inherited</U> handlers
     * @param l root logger from which to start replacing (inclusive) - may NOT be null
     * @param h new handler to use
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),value=previous handlers(array of {@link Handler}-s)
     * @see #replaceRecursiveHandlers(Logger, boolean, Handler...)
     */
    public static Map<String,Handler[]> replaceRecursiveHandler (Logger l, Handler h)
    {
        return replaceRecursiveHandler(l.getName(), h);
    }
    /**
     * Replaces the handlers of <U>all</U> loggers
     * @param replaceInherited if TRUE then handlers are replaced even if
     * inherited. Otherwise, if no handlers, then nothing done. <B>Note:</B>
     * unless you <U>really</U> know what you are doing, pass FALSE value
     * @param h new handlers to use - may NOT be null/empty, but may contain
     * null elements - provided <U>at least <B>one</B></U> non-null element
     * is found (otherwise exception is thrown - see below)
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),value=previous handlers(array of {@link Handler}-s)
     * @see #replaceRecursiveHandlers(String, boolean, Handler...)
     */
    public static Map<String,Handler[]> replaceRecursiveHandlers (boolean replaceInherited, Handler ... h)
    {
        return replaceRecursiveHandlers((String) null, replaceInherited, h);
    }
    /**
     * Replaces the handlers of <U>all</U> loggers
     * @param h new handlers to use - may NOT be null/empty, but may contain
     * null elements - provided <U>at least <B>one</B></U> non-null element
     * is found (otherwise exception is thrown - see below)
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),value=previous handlers(array of {@link Handler}-s)
     * @see #replaceRecursiveHandlers(boolean, Handler...)
     */
    public static Map<String,Handler[]> replaceRecursiveHandlers (Handler... h)
    {
        return replaceRecursiveHandlers(false, h);
    }
    /**
     * Replaces the handlers of <U>all</U> loggers
     * @param replaceInherited if TRUE then handlers are replaced even if
     * inherited. Otherwise, if no handlers, then nothing done. <B>Note:</B>
     * unless you <U>really</U> know what you are doing, pass FALSE value
     * @param h new handler to use - may NOT be null/empty
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),value=previous handlers(array of {@link Handler}-s)
     * @see #replaceRecursiveHandlers(boolean, Handler...)
     */
    public static Map<String,Handler[]> replaceRecursiveHandler (boolean replaceInherited, Handler h)
    {
        return replaceRecursiveHandlers(replaceInherited, h);
    }
    /**
     * Replaces the handlers of <U>all</U> loggers
     * @param h new handler to use - may NOT be null/empty
     * @return A {@link Map} of replaced loggers - key=logger name({@link String}),value=previous handlers(array of {@link Handler}-s)
     * @see #replaceRecursiveHandler(boolean, Handler)
     */
    public static Map<String,Handler[]> replaceRecursiveHandler (Handler h)
    {
        return replaceRecursiveHandler(false, h);
    }
}
