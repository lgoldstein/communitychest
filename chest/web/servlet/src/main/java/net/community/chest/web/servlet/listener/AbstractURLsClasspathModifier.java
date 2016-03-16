/*
 *
 */
package net.community.chest.web.servlet.listener;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.community.chest.lang.ExceptionUtil;

/**
 * Provides a {@link ServletContextListener#contextInitialized(ServletContextEvent)} implementation
 * that adds {@link URL}-s to the current {@link Thread#getContextClassLoader()}
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 6, 2011 10:29:10 AM
 */
public abstract class AbstractURLsClasspathModifier implements ServletContextListener {
    protected AbstractURLsClasspathModifier ()
    {
        super();
    }
    /**
     * @return A {@link Collection} of {@link URL}-s to be <U>added</U> to the
     * current {@link Thread#getContextClassLoader()} - ignored if <code>null</code>/empty
     */
    protected abstract Collection<? extends URL> getClasspathURLs ();
    /**
     * @return The {@link ClassLoader} to be modified by adding to it the
     * {@link URL}-s specified by {@link #getClasspathURLs()} - default={@link Thread#getContextClassLoader()}
     */
    protected ClassLoader getModifiableClassLoader ()
    {
        final Thread    t=Thread.currentThread();
        return t.getContextClassLoader();
    }
    /*
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized (ServletContextEvent sce)
    {
        addURLsToClasspath(getClasspathURLs(), getModifiableClassLoader());
    }
    /**
     * @param urls A {@link Collection} of {@link URL}-s to be <U>added</U> to
     * the provide {@link ClassLoader} - ignored if <code>null</code>/empty
     * @param loader The {@link ClassLoader} instance to add the {@link URL}-s
     * to. <B>Note:</B> the default implementation assumes this is a {@link URLClassLoader}
     * and invokes (via reflection API} its <code>addURL</code> method for each
     * of the provided URL(s)
     * @return The {@link Collection} of actually added {@link URL}-s - default
     * is to return the same as the input
     */
    protected Collection<? extends URL> addURLsToClasspath (final Collection<? extends URL> urls, final ClassLoader loader)
    {
        if ((urls == null) || (urls.size() <= 0))
            return urls;

        try
        {
            final Method    addMethod=URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            if (!addMethod.isAccessible())
                addMethod.setAccessible(true);

            for (final URL url : urls)
            {
                if (url == null)
                    continue;    // debug breakpoint

                addMethod.invoke(loader, url);
            }
        }
        catch (Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e, true);
        }

        return urls;
    }
    /*
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed (ServletContextEvent sce)
    {
        // do nothing
    }
}
