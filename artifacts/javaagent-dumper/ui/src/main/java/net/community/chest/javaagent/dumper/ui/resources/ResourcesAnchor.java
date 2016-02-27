/*
 * 
 */
package net.community.chest.javaagent.dumper.ui.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 11:03:16 AM
 */
public final class ResourcesAnchor extends BaseAnchor {
	private ResourcesAnchor ()
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
