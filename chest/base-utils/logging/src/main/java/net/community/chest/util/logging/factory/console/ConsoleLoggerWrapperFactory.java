package net.community.chest.util.logging.factory.console;

import java.util.Map;
import java.util.TreeMap;

import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.LoggerWrapperFactory;

/**
 * Copyright 2007 as per GPLv2
 *
 * Displays all log messages to the console STDOUT/STDERR (according to
 * property)
 *
 * @author Lyor G.
 * @since Jun 26, 2007 1:29:31 PM
 */
public final class ConsoleLoggerWrapperFactory implements LoggerWrapperFactory {
    public ConsoleLoggerWrapperFactory ()
    {
        super();
    }

    private static final Map<String,LoggerWrapper>    _logsMap=new TreeMap<String, LoggerWrapper>(String.CASE_INSENSITIVE_ORDER);
    private static final LoggerWrapper createWrapper (
            final Class<?> logClass, final String orgName, final String clsIndex)
    {
        final Class<?>    effClass=
            (null == logClass) /* should not happen */ ? ConsoleLoggerWrapperFactory.class : logClass;
        final String    effName=
            ((null == orgName) || (orgName.length() <= 0)) ? effClass.getName() : orgName,
                        logName=
            ((null == clsIndex) || (clsIndex.length() <= 0)) ? effName : effName + "[" + clsIndex + "]";

        LoggerWrapper    w=null;
        synchronized(_logsMap)
        {
            if (null == (w=_logsMap.get(logName)))
            {
                w = new ConsoleLoggerWrapper(effClass, orgName, clsIndex);
                _logsMap.put(logName, w);
            }
        }

        return w;
    }
    /*
     * @see net.community.chest.util.logging.LoggerWrapperFactory#getLogger(java.lang.Class, java.lang.String, java.lang.String)
     */
    @Override
    public LoggerWrapper getLogger (final Class<?> logClass, final String logName, final String clsIndex)
    {
        return createWrapper(logClass, logName, clsIndex);
    }
    /*
     * @see net.community.chest.util.logging.LoggerWrapperFactory#setLoggerComponentLevel(java.lang.String, net.community.chest.util.logging.LogLevelWrapper)
     */
    @Override
    public void setLoggerComponentLevel (String logName, LogLevelWrapper level)
    {
        ConsoleLoggerWrapper.setOutputLevel(level);
    }
}
