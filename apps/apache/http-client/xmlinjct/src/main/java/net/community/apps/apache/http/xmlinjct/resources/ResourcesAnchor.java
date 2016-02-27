/*
 * 
 */
package net.community.apps.apache.http.xmlinjct.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 11:53:33 AM
 */
public final class ResourcesAnchor extends BaseAnchor {
	private ResourcesAnchor () // no instance
	{
		super();
	}

	private static ResourcesAnchor	_instance	/* =null */;
	public static synchronized ResourcesAnchor getInstance ()
	{
		if (null == _instance)
			_instance = new ResourcesAnchor();
		return _instance;
	}
}
