/*
 *
 */
package net.community.apps.apache.maven.conv2maven.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Oct 25, 2010 1:49:49 PM
 */
public class ResourcesAnchor extends BaseAnchor {
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
