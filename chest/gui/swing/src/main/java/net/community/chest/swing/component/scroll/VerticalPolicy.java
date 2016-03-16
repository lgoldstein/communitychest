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
 * @since Dec 3, 2008 3:53:19 PM
 */
public enum VerticalPolicy {
    BYNEED(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED),
    NEVER(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER),
    ALWAYS(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    private final int    _v;
    public final int getPolicy ()
    {
        return _v;
    }

    VerticalPolicy (int v)
    {
        _v = v;
    }

    public static final List<VerticalPolicy>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final VerticalPolicy fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final VerticalPolicy fromPolicy (final int p)
    {
        for (final VerticalPolicy v : VALUES)
        {
            if ((v != null) && (v.getPolicy() == p))
                return v;
        }

        return null;
    }
}
