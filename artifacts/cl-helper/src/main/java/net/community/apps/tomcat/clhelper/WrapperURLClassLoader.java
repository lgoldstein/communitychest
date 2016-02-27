/*
 * 
 */
package net.community.apps.tomcat.clhelper;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 29, 2011 9:35:56 AM
 */
public class WrapperURLClassLoader extends URLClassLoader {
	private static final URL[]	EMPTY_URLS=new URL[0];
	public WrapperURLClassLoader (ClassLoader parent)
	{
		super(EMPTY_URLS, parent);
	}
}
