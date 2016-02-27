/*
 * 
 */
package net.community.chest.lang;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Collection;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 21, 2009 9:47:42 AM
 */
public class ExtendedURLClassLoader extends URLClassLoader {
	public ExtendedURLClassLoader (URL ... urls)
	{
		super(urls);
	}

	public static final URL[]	NO_URLS=new URL[0];
	public ExtendedURLClassLoader ()
	{
		this(NO_URLS);
	}

	public ExtendedURLClassLoader (Collection<? extends URL> urls)
	{
		this(((null == urls) || (urls.size() <= 0)) ? NO_URLS : urls.toArray(NO_URLS));
	}

	public ExtendedURLClassLoader (ClassLoader parent, URL ... urls)
	{
		super(urls, parent);
	}

	public ExtendedURLClassLoader (ClassLoader parent, Collection<? extends URL> urls)
	{
		this(parent, ((null == urls) || (urls.size() <= 0)) ? NO_URLS : urls.toArray(NO_URLS));
	}

	public ExtendedURLClassLoader (ClassLoader parent, URLStreamHandlerFactory factory, URL ... urls)
	{
		super(urls, parent, factory);
	}

	public ExtendedURLClassLoader (ClassLoader parent, URLStreamHandlerFactory factory, Collection<? extends URL> urls)
	{
		this(parent, factory, ((null == urls) || (urls.size() <= 0)) ? NO_URLS : urls.toArray(NO_URLS));
	}

	/* Promote to public
	 * @see java.net.URLClassLoader#addURL(java.net.URL)
	 */
	@Override
	public void addURL (URL url)
	{
		super.addURL(url);
	}

	private static Method	_addURLMethod;
	private static final synchronized Method getAddURLMethod () throws Exception
	{
		if (null == _addURLMethod)
			_addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		return _addURLMethod;
	}

	public static final <L extends URLClassLoader> L addURL (L cl, Collection<? extends URL> urls)
	{
		if ((null == cl) || (null == urls) || (urls.size() <= 0))
			return cl;

		if (cl instanceof ExtendedURLClassLoader)
		{
			final ExtendedURLClassLoader	ecl=(ExtendedURLClassLoader) cl;
			for (final URL url : urls)
			{
				if (null == url)
					continue;
				ecl.addURL(url);
			}
		}
		else
		{
			try
			{
				final Method	m=getAddURLMethod();
				if (!m.isAccessible())
					m.setAccessible(true);

				for (final URL url : urls)
				{
					if (null == url)
						continue;
					m.invoke(cl, url);
				}
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}

		return cl;
	}

	public static final <L extends URLClassLoader> L addURL (L cl, URL ... urls)
	{
		return ((null == cl) || (null == urls) || (urls.length <= 0)) ? cl : addURL(cl, Arrays.asList(urls));
	}
}
