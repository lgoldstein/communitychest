package net.community.chest.util.logging.factory.empty;

import java.util.Map;
import java.util.TreeMap;

import net.community.chest.util.logging.AbstractLoggerWrapper;
import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.LoggerWrapperFactory;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 26, 2007 1:14:44 PM
 */
public final class EmptyLoggerWrapperFactory implements LoggerWrapperFactory {
    private static final class EmptyLoggerWrapper extends AbstractLoggerWrapper {
        protected EmptyLoggerWrapper (final Class<?> logClass, final String logName, final String clsIndex)
        {
            super(logClass, logName, clsIndex);
        }
        /*
         * @see net.community.chest.util.logging.LoggerWrapper#isEnabledFor(net.community.chest.util.logging.LogLevelWrapper)
         */
        @Override
        public boolean isEnabledFor (LogLevelWrapper l)
        {
            return false;
        }
        /*
         * @see net.community.chest.util.logging.LoggerWrapper#setEnabledFor(net.community.chest.util.logging.LogLevelWrapper)
         */
        @Override
        public boolean setEnabledFor (LogLevelWrapper l)
        {
            return (l != null);
        }
        /*
         * @see net.community.chest.util.logging.LoggerWrapper#log(net.community.chest.util.logging.LogLevelWrapper, java.lang.String, java.lang.Throwable)
         */
        @Override
        public String log (LogLevelWrapper l, String msg, Throwable t)
        {
            return msg;
        }
    }

    private static final Map<String,LoggerWrapper>    _logsMap=new TreeMap<String, LoggerWrapper>(String.CASE_INSENSITIVE_ORDER);
    public static final LoggerWrapper createWrapper (
            final Class<?> logClass, final String orgName, final String clsIndex)
    {
        final Class<?>    effClass=
            (null == logClass) /* should not happen */ ? EmptyLoggerWrapperFactory.class : logClass;
        final String    effName=
            ((null == orgName) || (orgName.length() <= 0)) ? effClass.getName() : orgName,
                        logName=
            ((null == clsIndex) || (clsIndex.length() <= 0)) ? effName : effName + "[" + clsIndex + "]";

        LoggerWrapper    w=null;
        synchronized(_logsMap)
        {
            if (null == (w=_logsMap.get(logName)))
            {
                w = new EmptyLoggerWrapper(effClass, orgName, clsIndex);
                _logsMap.put(logName, w);
            }
        }

        return w;
    }

    public EmptyLoggerWrapperFactory ()
    {
        super();
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
        // do nothing
    }
}
