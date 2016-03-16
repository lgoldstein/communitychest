/*
 *
 */
package net.community.apps.tools.commenter.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 30, 2009 11:18:07 AM
 */
public class ResourcesAnchor extends BaseAnchor {
    private ResourcesAnchor () // no instance
    {
        super();
    }

    private static ResourcesAnchor    _instance    /* =null */;
    public static synchronized ResourcesAnchor getInstance ()
    {
        if (null == _instance)
            _instance = new ResourcesAnchor();
        return _instance;
    }
}
