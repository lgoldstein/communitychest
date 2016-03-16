/*
 *
 */
package net.community.chest.web.servlet.listener;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.community.chest.io.FileUtil;
import net.community.chest.lang.ExceptionUtil;

/**
 * Provides a {@link ServletContextListener#contextInitialized(ServletContextEvent)} implementation
 * that adds {@link File}-s to the current {@link Thread#getContextClassLoader()}
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 6, 2011 10:38:27 AM
 */
public abstract class AbstractFilesClasspathModifier extends AbstractURLsClasspathModifier {
    protected AbstractFilesClasspathModifier ()
    {
        super();
    }
    /**
     * @return A {@link Collection} of {@link File}-s to be added to the
     * current {@link Thread#getContextClassLoader()} - ignored if <code>null</code>/empty
     */
    protected abstract Collection<? extends File> getClasspathFiles ();
    /*
     * @see net.community.chest.web.servlet.listener.AbstractURLsClasspathModifier#getClasspathURLs()
     */
    @Override
    protected Collection<? extends URL> getClasspathURLs ()
    {
        try
        {
            return FileUtil.toURL(getClasspathFiles());
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
}
