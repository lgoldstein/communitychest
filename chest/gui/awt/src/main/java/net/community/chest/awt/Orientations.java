package net.community.chest.awt;

import java.awt.ComponentOrientation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate some {@link ComponentOrientation}-s as {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Mar 20, 2008 8:19:38 AM
 */
public enum Orientations {
    LEFT_TO_RIGHT(ComponentOrientation.LEFT_TO_RIGHT, "left-to-right"),
    RIGHT_TO_LEFT(ComponentOrientation.RIGHT_TO_LEFT, "right-to-left"),
    UNKNOWN(ComponentOrientation.UNKNOWN, "unknown");

    private final ComponentOrientation    _orient;
    public final ComponentOrientation getOrientation ()
    {
        return _orient;
    }

    private final String    _display;
    /*
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString ()
    {
        return _display;
    }

    Orientations (ComponentOrientation orient, String display)
    {
        _orient = orient;
        _display = display;
    }

    public static final List<Orientations>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final Orientations fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final Orientations fromComponentOrientation (final ComponentOrientation o)
    {
        if (null == o)
            return null;

        for (final Orientations v : VALUES)
        {
            if ((v != null) && (v.getOrientation() == o))
                return v;
        }

        return null;
    }
}
