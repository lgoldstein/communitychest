/*
 * 
 */
package net.community.apps.apache.ant.antrunner;

import java.net.URL;
import java.util.Collection;

import net.community.chest.io.jar.BaseURLClassLoader;

public class ANTJarsClassLoader extends BaseURLClassLoader {
	protected ANTJarsClassLoader (ClassLoader parent, Collection<? extends URL>	urls)
	{
		super(parent, urls.toArray(new URL[urls.size()]));
	}

	public ANTJarsClassLoader (ClassLoader parent)
	{
		super(parent, new URL[0]);
	}
	/*
	 * @see java.net.URLClassLoader#findClass(java.lang.String)
	 */
	@Override
	protected Class<?> findClass (String name) throws ClassNotFoundException
	{
		if ((name != null) && (name.length() > 0) && name.startsWith("org.apache.tools.ant"))
			return super.findClass(name);	// debug breakpoint

		return super.findClass(name);
	}
}