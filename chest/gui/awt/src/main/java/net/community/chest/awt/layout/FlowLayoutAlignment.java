package net.community.chest.awt.layout;

import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Available {@link java.awt.FlowLayout} alignment values as {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Mar 19, 2008 1:05:39 PM
 */
public enum FlowLayoutAlignment {
    LEFT(FlowLayout.LEFT),
    CENTER(FlowLayout.CENTER),
    RIGHT(FlowLayout.RIGHT),
    LEADING(FlowLayout.LEADING),
    TRAILING(FlowLayout.TRAILING);

    private final int    _a;
    public final int getAlignment ()
    {
        return _a;
    }

    FlowLayoutAlignment (final int a)
    {
        _a = a;
    }

    public static final List<FlowLayoutAlignment>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final FlowLayoutAlignment fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final FlowLayoutAlignment fromAlignment (final int a)
    {
        for (final FlowLayoutAlignment v : VALUES)
        {
            if ((v != null) && (v.getAlignment() == a))
                return v;
        }

        return null;
    }
}
