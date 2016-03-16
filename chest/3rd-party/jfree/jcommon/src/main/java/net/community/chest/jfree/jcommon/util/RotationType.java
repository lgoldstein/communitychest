/*
 *
 */
package net.community.chest.jfree.jcommon.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.util.Rotation;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate the {@link Rotation} values into an {@link Enum}<?P>
 * @author Lyor G.
 * @since Feb 1, 2009 2:48:24 PM
 */
public enum RotationType {
    CLOCKWISE(Rotation.CLOCKWISE),
    ANTICLOCKWISE(Rotation.ANTICLOCKWISE);

    private final Rotation    _r;
    public final Rotation getRotation ()
    {
        return _r;
    }

    RotationType (Rotation r)
    {
        _r = r;
    }

    public static final List<RotationType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final RotationType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final RotationType fromRotation (final Rotation r)
    {
        if (null == r)
            return null;

        for (final RotationType v : VALUES)
        {
            if ((v != null) && r.equals(v.getRotation()))
                return v;
        }

        return null;
    }
}
