/*
 * 
 */
package net.community.apps.tools.filesync.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 15, 2009 2:57:34 PM
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
