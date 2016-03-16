package net.community.apps.apache.ant.antrunner.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>"Anchor" class used to access various GUI resources - e.g.,
 * XML configuration files, icons, etc.</P>
 *
 * @author Lyor G.
 * @since Jul 22, 2007 8:53:09 AM
 */
public final class ResourcesAnchor extends BaseAnchor {
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
