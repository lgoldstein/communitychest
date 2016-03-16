/*
 *
 */
package net.community.chest.jfree.jcommon.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.util.SortOrder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 5, 2009 3:53:50 PM
 */
public enum SortOrderEnum {
    ASCENDING(SortOrder.ASCENDING),
    DESCENDING(SortOrder.DESCENDING);

    private final SortOrder    _o;
    public final SortOrder getSortOrder ()
    {
        return _o;
    }

    SortOrderEnum (SortOrder o)
    {
        _o = o;
    }

    public static final List<SortOrderEnum>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final SortOrderEnum fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final SortOrderEnum fromSortOrder (final SortOrder o)
    {
        if (null == o)
            return null;

        for (final SortOrderEnum  v : VALUES)
        {
            if ((v != null) && o.equals(v.getSortOrder()))
                return v;
        }

        return null;
    }
}
