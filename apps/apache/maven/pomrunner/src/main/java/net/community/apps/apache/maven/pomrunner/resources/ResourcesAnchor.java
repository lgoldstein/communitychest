package net.community.apps.apache.maven.pomrunner.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Serves as access "anchor" for the various application resources</P>
 *
 * @author Lyor G.
 * @since Aug 8, 2007 2:04:24 PM
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
