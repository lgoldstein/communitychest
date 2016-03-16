package net.community.apps.common.test.resources;

import net.community.apps.common.resources.BaseAnchor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 19, 2008 11:31:52 AM
 */
public final class TestResourcesAnchor extends BaseAnchor {
    private TestResourcesAnchor ()
    {
        // no instance
    }

    private static TestResourcesAnchor    _instance    /* =null */;
    public static final synchronized TestResourcesAnchor getInstance ()
    {
        if (null == _instance)
            _instance = new TestResourcesAnchor();
        return _instance;
    }
}
