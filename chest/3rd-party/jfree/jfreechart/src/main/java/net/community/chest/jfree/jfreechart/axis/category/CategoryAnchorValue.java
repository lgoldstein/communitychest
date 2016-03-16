/*
 *
 */
package net.community.chest.jfree.jfreechart.axis.category;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.axis.CategoryAnchor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 5, 2009 3:59:49 PM
 */
public enum CategoryAnchorValue {
    START(CategoryAnchor.START),
    MIDDLE(CategoryAnchor.MIDDLE),
    END(CategoryAnchor.END);

    private final CategoryAnchor    _a;
    public final CategoryAnchor getAnchor ()
    {
        return _a;
    }

    CategoryAnchorValue (CategoryAnchor a)
    {
        _a = a;
    }

    public static final List<CategoryAnchorValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final CategoryAnchorValue fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final CategoryAnchorValue fromAnchor (final CategoryAnchor a)
    {
        if (null == a)
            return null;

        for (final CategoryAnchorValue  v : VALUES)
        {
            if ((v != null) && a.equals(v.getAnchor()))
                return v;
        }

        return null;
    }
}
