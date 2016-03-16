/*
 *
 */
package net.community.chest.jfree.jcommon.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.util.TableOrder;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulates the {@link TableOrder} values as a proper {@link Enum}</P>
 *
 * @author Lyor G.
 * @since May 26, 2009 12:38:49 PM
 */
public enum TableOrderEnum {
    ROW(TableOrder.BY_ROW),
    COL(TableOrder.BY_COLUMN);

    private final TableOrder    _o;
    public final TableOrder getTableOrder ()
    {
        return _o;
    }

    TableOrderEnum (TableOrder o)
    {
        _o = o;
    }

    public static final List<TableOrderEnum>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final TableOrderEnum fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final TableOrderEnum fromTableOrder (final TableOrder o)
    {
        if (null == o)
            return null;

        for (final TableOrderEnum  v : VALUES)
        {
            if ((v != null) && o.equals(v.getTableOrder()))
                return v;
        }

        return null;
    }
}
