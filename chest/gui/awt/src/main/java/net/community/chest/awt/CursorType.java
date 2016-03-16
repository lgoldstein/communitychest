package net.community.chest.awt;

import java.awt.Cursor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates some well-known {@link Cursor} values into an {@link Enum}</P>
 * @author Lyor G.
 * @since Mar 20, 2008 11:06:24 AM
 */
public enum CursorType {
    DEFAULT(Cursor.DEFAULT_CURSOR),
    CROSSHAIR(Cursor.CROSSHAIR_CURSOR),
    TEXT(Cursor.TEXT_CURSOR),
    WAIT(Cursor.WAIT_CURSOR),
    SOUTHWEST(Cursor.SW_RESIZE_CURSOR),
    SOUTHEAST(Cursor.SE_RESIZE_CURSOR),
    NORTHWEST(Cursor.NW_RESIZE_CURSOR),
    NORTHEAST(Cursor.NE_RESIZE_CURSOR),
    NORTH(Cursor.N_RESIZE_CURSOR),
    SOUTH(Cursor.S_RESIZE_CURSOR),
    WEST(Cursor.W_RESIZE_CURSOR),
    EAST(Cursor.E_RESIZE_CURSOR),
    HAND(Cursor.HAND_CURSOR),
    MOVE(Cursor.MOVE_CURSOR);

    private final int    _t;
    public final int getCursorType ()
    {
        return _t;
    }

    public final Cursor getCursor ()
    {
        return Cursor.getPredefinedCursor(getCursorType());
    }

    CursorType (final int t)
    {
        _t = t;
    }

    public static final List<CursorType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final CursorType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final CursorType fromCursorType (final int t)
    {
        for (final CursorType v : VALUES)
        {
            if ((v != null) && (v.getCursorType() == t))
                return v;
        }

        return null;
    }

    public static final CursorType fromCursor (final Cursor c)
    {
        if (null == c)
            return null;

        return fromCursorType(c.getType());
    }
}
