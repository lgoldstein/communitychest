/*
 *
 */
package jnlp.sample.util.log;

import java.util.ResourceBundle;

import javax.servlet.ServletConfig;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 21, 2009 10:12:51 AM
 */
public final class LoggerFactory {
    private LoggerFactory ()
    {
        // no instance
    }

    private static LoggerManager    _mgr    /* =null */;
    public static final LoggerManager getLoggerManager ()
    {
        return _mgr;
    }
    // returns previous manager
    public static final synchronized LoggerManager setLoggerManager (LoggerManager mgr)
    {
        final LoggerManager    prev=_mgr;
        _mgr = mgr;
        return prev;
    }

    public static final synchronized void initLogger (ServletConfig config, ResourceBundle resources)
    {
        LoggerManager    mgr=getLoggerManager();
        if (null == mgr)
        {
            final String    mgrClass=System.getProperty("jnlp.sample.util.log.manager.class", DefaultLoggerManager.class.getName());
            try
            {
                final Thread        t=Thread.currentThread();
                final ClassLoader    cl=t.getContextClassLoader();
                final Class<?>        c=cl.loadClass(mgrClass);
                mgr = (LoggerManager) c.newInstance();
            }
            catch(Exception e)
            {
                mgr = new DefaultLoggerManager();
            }

            setLoggerManager(mgr);
        }

        mgr.initLogger(config, resources);
    }

    public static final Logger getLogger (String loggerName)
    {
        final LoggerManager    mgr=getLoggerManager();
        return (null == mgr) ? null : mgr.getLogger(loggerName);
     }

    public static final Logger getLogger (final Class<?> c, final String clsIndex)
    {
        final String    cn=(null == c) ? null : c.getName(),
                        ln=((null == clsIndex) || (clsIndex.length() <= 0)) ? cn : cn + "[" + clsIndex + "]";
        return getLogger(ln);
    }

    public static final Logger getLogger (final Class<?> c)
    {
        return getLogger(c, null);
    }
}
