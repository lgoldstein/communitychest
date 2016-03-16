/*
 *
 */
package net.community.apps.tools.jardiff.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 2, 2011 8:51:31 AM
 */
public final class ResourcesAnchor extends BaseAnchor {
    private ResourcesAnchor ()
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
