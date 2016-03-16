package net.community.chest.apache.log4j.factory;

import java.util.NoSuchElementException;

import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.LoggerWrapperFactory;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Implements {@link LoggerWrapperFactory} interface using log4j logger</P>
 *
 * @author Lyor G.
 * @since Sep 30, 2007 3:27:35 PM
 */
public final class Log4jLoggerWrapperFactory implements LoggerWrapperFactory {
    public Log4jLoggerWrapperFactory ()
    {
        super();
    }
    /*
     * @see net.community.chest.util.logging.LoggerWrapperFactory#getLogger(java.lang.Class, java.lang.String, java.lang.String)
     */
    @Override
    public LoggerWrapper getLogger (final Class<?> logClass, final String logName, final String clsIndex)
    {
        return new Log4jLoggerWrapper(logClass, logName, clsIndex);
    }
    /*
     * @see net.community.chest.util.logging.LoggerWrapperFactory#setLoggerComponentLevel(java.lang.String, net.community.chest.util.logging.LogLevelWrapper)
     */
    @Override
    public void setLoggerComponentLevel (String logName, LogLevelWrapper level)
    {
        final org.apache.log4j.Level    log4jLevel=
            Log4jLoggerWrapper.fromJDKLevel(level);
        final org.apache.log4j.Logger    logger=
            ((null == logName) || (logName.length() <= 0) || (null == log4jLevel))    // should not happen
                    ? null : org.apache.log4j.LogManager.exists(logName);
        if (null == logger)
            throw new NoSuchElementException("setLoggerComponentLevel(" + logName + "[" + level + "]) no logger instance");
        logger.setLevel(log4jLevel);
    }
    /**
     * Replaces the current {@link LoggerWrapperFactory} with a {@link Log4jLoggerWrapperFactory}
     * instance if not already such
     * @return The replaced {@link Log4jLoggerWrapperFactory} - <B>Note:</V> a
     * {@link Log4jLoggerWrapperFactory} instance indicates no replacement
     * (since current {@link LoggerWrapperFactory} is already a
     * {@link Log4jLoggerWrapperFactory} instance).
     * @throws Exception If replacement failed
     */
    public static final LoggerWrapperFactory replaceCurrentFactory () throws Exception
    {
        final LoggerWrapperFactory    cur=WrapperFactoryManager.getFactory(false);
        if (!(cur instanceof Log4jLoggerWrapperFactory))
            WrapperFactoryManager.setFactory(new Log4jLoggerWrapperFactory());
        return cur;
    }

}
