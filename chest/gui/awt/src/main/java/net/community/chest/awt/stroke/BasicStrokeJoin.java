/*
 *
 */
package net.community.chest.awt.stroke;

import java.awt.BasicStroke;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate {@link BasicStroke} join values to {@link Enum}</P>
 *
 * @author Lyor G.
 * @since Feb 1, 2009 4:26:31 PM
 */
public enum BasicStrokeJoin {
    MITER(BasicStroke.JOIN_MITER),
    ROUND(BasicStroke.JOIN_ROUND),
    BEVEL(BasicStroke.JOIN_BEVEL);

    private final int    _j;
    public final int getJoin ()
    {
        return _j;
    }

    BasicStrokeJoin (int j)
    {
        _j = j;
    }

    public static final List<BasicStrokeJoin>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final BasicStrokeJoin fromString (String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final BasicStrokeJoin fromJoin (final int j)
    {
        for (final BasicStrokeJoin v : VALUES)
        {
            if ((v != null) && (v.getJoin() == j))
                return v;
        }

        return null;
    }

}
