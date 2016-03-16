/*
 *
 */
package net.community.chest.swing.component.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JTable;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates the values for {@link JTable#setAutoResizeMode(int)} as {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Dec 31, 2008 8:02:32 AM
 */
public enum TableAutoResizeMode {
    OFF(JTable.AUTO_RESIZE_OFF),
    NEXT(JTable.AUTO_RESIZE_NEXT_COLUMN),
    SUBSEQ(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS),
    LASTC(JTable.AUTO_RESIZE_LAST_COLUMN),
    ALL(JTable.AUTO_RESIZE_ALL_COLUMNS);

    private final int    _mode;
    public final int getMode ()
    {
        return _mode;
    }

    TableAutoResizeMode (final int m)
    {
        _mode = m;
    }

    public static final List<TableAutoResizeMode>    VALUES=Collections.unmodifiableList(Arrays.asList(values()))    /* =null */;
    public static final TableAutoResizeMode fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final TableAutoResizeMode fromMode (final int m)
    {
        for (final TableAutoResizeMode v : VALUES)
        {
            if ((v != null) && (v.getMode() == m))
                return v;
        }

        return null;
    }

}
