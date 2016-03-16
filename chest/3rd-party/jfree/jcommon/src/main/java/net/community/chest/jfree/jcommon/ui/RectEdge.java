/*
 *
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.ui.RectangleEdge;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 27, 2009 3:41:29 PM
 */
public enum RectEdge {
    TOP(RectangleEdge.TOP),
    BOTTOM(RectangleEdge.BOTTOM),
    LEFT(RectangleEdge.LEFT),
    RIGHT(RectangleEdge.RIGHT);

    private final RectangleEdge    _edge;
    public final RectangleEdge getEdge ()
    {
        return _edge;
    }

    RectEdge (RectangleEdge e)
    {
        _edge = e;
    }

    public static final List<RectEdge>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final RectEdge fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final RectEdge fromEdge (final RectangleEdge a)
    {
        if (null == a)
            return null;

        for (final RectEdge  v : VALUES)
        {
            if ((v != null) && a.equals(v.getEdge()))
                return v;
        }

        return null;
    }
}
