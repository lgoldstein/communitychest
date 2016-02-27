/*
 * 
 */
package net.community.chest.io.jar;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.util.SortedSet;

import net.community.chest.io.url.ByExternalFormURLComparator;
import net.community.chest.io.url.URLComparator;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 2, 2009 10:18:27 AM
 */
public class BaseURLClassLoader extends URLClassLoader {
	public BaseURLClassLoader (URL ... urls)
	{
		super(urls);
	}

	public BaseURLClassLoader (Collection<? extends URL> urls)
	{
		this(((urls == null) || urls.isEmpty()) ? null : urls.toArray(new URL[urls.size()]));
	}

	public BaseURLClassLoader (ClassLoader parent, URL ... urls)
	{
		super(urls, parent);
	}

	public BaseURLClassLoader (ClassLoader parent, Collection<? extends URL> urls)
	{
		this(parent, ((urls == null) || urls.isEmpty()) ? null : urls.toArray(new URL[urls.size()]));
	}

	public BaseURLClassLoader (ClassLoader parent, URLStreamHandlerFactory factory, URL ... urls)
	{
		super(urls, parent, factory);
	}

	public BaseURLClassLoader (ClassLoader parent, URLStreamHandlerFactory factory, Collection<? extends URL> urls)
	{
		this(parent, factory, ((urls == null) || urls.isEmpty()) ? null : urls.toArray(new URL[urls.size()]));
	}

	public SortedSet<URL> getCurrentURLs ()
	{
		return SetsUtils.setOf(ByExternalFormURLComparator.BY_EXTFORM_ASCENDING, getURLs());
	}

	public boolean addURL (URL url, boolean ignoreIfExists)
	{
		if (null == url)
			return false;

		if (ignoreIfExists)
		{
			final Collection<? extends URL>	urls=getCurrentURLs();
			if (urls.contains(url))
				return false;
		}

		super.addURL(url);
		return true;
	}
	/* NOTE: promoted to 'public' + ignores URL(s) that are already set
	 * @see java.net.URLClassLoader#addURL(java.net.URL)
	 */
	@Override
	public void addURL (URL url)
	{
		addURL(url, true);
	}
	// returns number of added URL(s)
	public int addAll (boolean ignoreIfExists, Collection<? extends URL> urls)
	{
		if ((null == urls) || (urls.size() <= 0))
			return 0;

		int	numAdded=0;
		for (final URL u : urls)
		{
			if (!addURL(u, ignoreIfExists))
				continue;	// debug breakpoint

			numAdded++;
		}

		return numAdded;
	}
	// returns number of added URL(s)
	public int addAll (Collection<? extends URL> urls)
	{
		return addAll(true, urls);
	}
	// returns number of added URL(s)
	public int addAll (boolean ignoreIfExists, URL ... urls)
	{
		if ((null == urls) || (urls.length <= 0))
			return 0;

		return addAll(ignoreIfExists, SetsUtils.setOf(URLComparator.ASCENDING, urls));

	}
	// returns number of added URL(s)
	public int addAll (URL ... urls)
	{
		return addAll(true, urls);
	}
}
