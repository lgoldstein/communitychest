/*
 *
 */
package net.community.chest.awt.layout.spring;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Spring;
import javax.swing.SpringLayout;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>And {@link Enum} encapsulating {@link SpringLayout} position values</P>
 *
 * @author Lyor G.
 * @since Apr 23, 2009 7:45:20 AM
 */
public enum SpringLayoutPosition {
    NORTH(SpringLayout.NORTH),
    SOUTH(SpringLayout.SOUTH),
    EAST(SpringLayout.EAST),
    WEST(SpringLayout.WEST);

    private final String    _position;
    public final String getPosition ()
    {
        return _position;
    }

    public final Spring getConstraint (SpringLayout l, Component c)
    {
        if ((null == l) || (null == c))
            return null;

        final String    p=getPosition();
        return l.getConstraint(p, c);
    }

    SpringLayoutPosition (String position)
    {
        _position = position;
    }

    public static final List<SpringLayoutPosition>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final SpringLayoutPosition fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final SpringLayoutPosition fromPosition (final String p)
    {
        if ((null == p) || (p.length() <= 0))
            return null;

        for (final SpringLayoutPosition v : VALUES)
        {
            final String    vp=(null == v) ? null : v.getPosition();
            if (p.equalsIgnoreCase(vp))
                return v;
        }

        return null;
    }
}
