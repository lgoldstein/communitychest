/*
 *
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.ui.Align;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate the {@link Align} values as {@link Enum}</P>
 *
 * @author Lyor G.
 * @since Jan 29, 2009 10:21:16 AM
 */
public enum AlignType {
   CENTER(Align.CENTER),
   TOP(Align.TOP),
   BOTTOM(Align.BOTTOM),
   LEFT(Align.LEFT),
   RIGHT(Align.RIGHT),
   TOPLEFT(Align.TOP_LEFT),
   TOPRIGHT(Align.TOP_RIGHT),
   BOTLEFT(Align.BOTTOM_LEFT),
   BOTRIGHT(Align.BOTTOM_RIGHT),
   HFIT(Align.FIT_HORIZONTAL),
   VFIT(Align.FIT_VERTICAL),
   FIT(Align.FIT),
   NORTH(Align.NORTH),
   SOUTH(Align.SOUTH),
   EAST(Align.EAST),
   WEST(Align.WEST),
   NORTHWEST(Align.NORTH_WEST),
   NORTHEAST(Align.NORTH_EAST),
   SOUTHWEST(Align.SOUTH_WEST),
   SOUTHEAST(Align.SOUTH_EAST);

    private final int    _a;
    public final int getAlignment ()
    {
        return _a;
    }

    AlignType (int a)
    {
        _a = a;
    }

    public static final List<AlignType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final AlignType fromString (final String name)
    {
        return CollectionsUtils.fromString(VALUES, name, false);
    }

    public static final AlignType fromAlignment (final int a)
    {
        for (final AlignType v : VALUES)
        {
            if ((v != null) && (v.getAlignment() == a))
                return v;
        }

        return null;
    }
}
