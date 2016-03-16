/*
 *
 */
package net.community.chest.swing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.SortOrder;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides some useful methods for {@link SortOrder}s
 * @author Lyor G.
 * @since Sep 25, 2008 9:48:15 AM
 */
public final class SortOrderValue {
    private SortOrderValue ()
    {
        // no instance
    }

    public static final List<SortOrder>    VALUES=Collections.unmodifiableList(Arrays.asList(SortOrder.values()));
    public static final SortOrder fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }
}
