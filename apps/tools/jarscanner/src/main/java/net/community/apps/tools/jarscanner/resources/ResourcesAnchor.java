/*
 * 
 */
package net.community.apps.tools.jarscanner.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 30, 2009 1:16:29 PM
 */
public class ResourcesAnchor extends BaseAnchor {
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
