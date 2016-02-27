/*
 * 
 */
package net.community.apps.tools.itext.pdfconcat.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2009 12:01:46 PM
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
