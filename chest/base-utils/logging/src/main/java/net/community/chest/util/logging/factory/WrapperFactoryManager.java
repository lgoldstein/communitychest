package net.community.chest.util.logging.factory;

import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.LoggerWrapperFactory;
import net.community.chest.util.logging.factory.console.ConsoleLoggerWrapperFactory;
import net.community.chest.util.logging.factory.empty.EmptyLoggerWrapperFactory;

/**
 * Copyright 2007 as per GPLv2
 *
 * The manager from which {@link LoggerWrapper} instance are retrieved
 * @author Lyor G.
 * @since Jun 26, 2007 1:07:06 PM
 */
public final class WrapperFactoryManager {
    private WrapperFactoryManager ()
    {
        // no instance
    }
    /**
     * Property name that controls the {@link LoggerWrapperFactory} class name
     * to be used. Default={@link ConsoleLoggerWrapperFactory} class name.
     */
    public static final String MANAGER_FACTORY_CLASS_PATH_PROPNAME=WrapperFactoryManager.class.getName().toLowerCase();

    private static LoggerWrapperFactory    _factory    /* =null */;
    public static final synchronized LoggerWrapperFactory getFactory (final boolean createIfNotExist) throws Exception
    {
        if ((null == _factory) && createIfNotExist)
        {
            final String    facName=System.getProperty(MANAGER_FACTORY_CLASS_PATH_PROPNAME, ConsoleLoggerWrapperFactory.class.getName());
            final Class<?>    facClass=ClassUtil.loadClassByName(facName);
            _factory = LoggerWrapperFactory.class.cast(facClass.newInstance());
        }

        return _factory;
    }

    public static final synchronized LoggerWrapperFactory getFactory () throws Exception
    {
        return getFactory(true);
    }

    // returns previous factory
    public static final synchronized LoggerWrapperFactory setFactory (final LoggerWrapperFactory fac) throws Exception
    {
        final LoggerWrapperFactory    prev=_factory;
        if (null == (_factory=fac))    // allow it anyway => next time getFactory is called, the default will be created
            throw new IllegalStateException("Null " + LoggerWrapperFactory.class.getSimpleName() + " instance N/A");
        return prev;
    }

    public static final LoggerWrapper getLogger (final Class<?> logClass,
                                                 final String    logName,
                                                 final String    clsIndex)
    {
        try
        {
            return getFactory().getLogger(logClass, logName, clsIndex);
        }
        catch(Exception e)
        {
            final LoggerWrapper    defWrap=
                EmptyLoggerWrapperFactory.createWrapper(logClass, logName, clsIndex);
            defWrap.error("getLogger(" + ((null == logClass) ? null : logClass.getName()) + ")[" + clsIndex + "] " + e.getClass().getName() + ": " + e.getMessage());
            return defWrap;
        }
    }

    public static final LoggerWrapper getLogger (final Class<?> logClass, final String clsIndex)
    {
        return getLogger(logClass, (null == logClass) ? null : logClass.getName(), clsIndex);
    }

    public static final LoggerWrapper getLogger (final Class<?> logClass)
    {
        return getLogger(logClass, null);
    }
}
