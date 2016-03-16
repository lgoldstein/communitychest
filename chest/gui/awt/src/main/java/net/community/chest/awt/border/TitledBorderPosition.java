/*
 *
 */
package net.community.chest.awt.border;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.border.TitledBorder;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to convert between <code>int</code> values and {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Oct 6, 2008 3:45:50 PM
 */
public enum TitledBorderPosition {
    ABOVE_TOP(TitledBorder.ABOVE_TOP),
    TOP(TitledBorder.TOP),
    BELOW_TOP(TitledBorder.BELOW_TOP),
    ABOVE_BOTTOM(TitledBorder.ABOVE_BOTTOM),
    BOTTOM(TitledBorder.BOTTOM),
    BELOW_BOTTOM(TitledBorder.BELOW_BOTTOM),
    DEFAULT(TitledBorder.DEFAULT_POSITION);

    private final int    _pos;
    public final int getPosition ()
    {
        return _pos;
    }

    TitledBorderPosition (final int pos)
    {
        _pos = pos;
    }

    public static final List<TitledBorderPosition>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final TitledBorderPosition fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final TitledBorderPosition fromPosition (final int pos)
    {
        for (final TitledBorderPosition v : VALUES)
        {
            if ((v != null) && (v.getPosition() == pos))
                return v;
        }

        return null;
    }
}
