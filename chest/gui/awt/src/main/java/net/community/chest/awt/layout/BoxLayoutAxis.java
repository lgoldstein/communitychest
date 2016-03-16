package net.community.chest.awt.layout;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.BoxLayout;

import net.community.chest.dom.DOMUtils;
import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate the available {@link javax.swing.BoxLayout} axis <code><I>int</I></code>
 * values as {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Mar 20, 2008 8:08:02 AM
 */
public enum BoxLayoutAxis {
    X(BoxLayout.X_AXIS),
    Y(BoxLayout.Y_AXIS),
    LINE(BoxLayout.LINE_AXIS),
    PAGE(BoxLayout.PAGE_AXIS);

    private final int    _x;
    public final int getAxis ()
    {
        return _x;
    }

    BoxLayoutAxis (final int x)
    {
        _x = x;
    }

    public static final List<BoxLayoutAxis>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final BoxLayoutAxis fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final BoxLayoutAxis fromAxis (final int a)
    {
        for (final BoxLayoutAxis v : VALUES)
        {
            if ((v != null) && (v.getAxis() == a))
                return v;
        }

        return null;
    }

    public static final String    BOX_LAYOUT_AXIS_ATTR="axis";
    public static final BoxLayoutAxis fromElement (Element elem) throws NoSuchElementException
    {
        final String    s=(null == elem) ? null : elem.getAttribute(BOX_LAYOUT_AXIS_ATTR);
        if ((null == s) || (s.length() <= 0))
            return null;

        BoxLayoutAxis    av=fromString(s);
        if (null == av)
        {
            final int    axis=Integer.parseInt(s);
            if (null == (av=fromAxis(axis)))
                throw new NoSuchElementException("fromElement(" + DOMUtils.toString(elem) + ") unknown axis value: " + s);
        }

        return av;
    }
}
