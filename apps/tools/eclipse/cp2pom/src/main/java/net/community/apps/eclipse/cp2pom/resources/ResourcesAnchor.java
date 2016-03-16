/*
 *
 */
package net.community.apps.eclipse.cp2pom.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 27, 2009 9:31:50 AM
 */
public class ResourcesAnchor  extends BaseAnchor {
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
