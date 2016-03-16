/*
 *
 */
package net.community.chest.swing.component.scroll;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ScrollPaneConstants;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 3, 2008 4:00:29 PM
 */
public enum HorizontalPolicy {
    BYNEED(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
    NEVER(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
    ALWAYS(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

    private final int    _v;
    public final int getPolicy ()
    {
        return _v;
    }

    HorizontalPolicy (int v)
    {
        _v = v;
    }

    public static final List<HorizontalPolicy>    VALUES=Collections.unmodifiableList(Arrays.asList(values()))    /* =null */;
    public static final HorizontalPolicy fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final HorizontalPolicy fromPolicy (final int p)
    {
        for (final HorizontalPolicy v : VALUES)
        {
            if ((v != null) && (v.getPolicy() == p))
                return v;
        }

        return null;
    }
}
