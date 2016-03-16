/*
 *
 */
package net.community.chest.swing.component.scroll;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ScrollPaneConstants;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulates the arguments to {@link javax.swing.JScrollPane#setCorner(String, java.awt.Component)}
 * method as {@link Enum}-s</P>
 * @author Lyor G.
 * @since Apr 21, 2009 9:22:00 AM
 */
public enum CornerPosition {
    LOLEFT(ScrollPaneConstants.LOWER_LEFT_CORNER),
    LORIGHT(ScrollPaneConstants.LOWER_RIGHT_CORNER),
    UPLEFT(ScrollPaneConstants.UPPER_LEFT_CORNER),
    UPRIGHT(ScrollPaneConstants.UPPER_RIGHT_CORNER),
    LOLEADING(ScrollPaneConstants.LOWER_LEADING_CORNER),
    LOTRAILING(ScrollPaneConstants.LOWER_TRAILING_CORNER),
    UPLEADING(ScrollPaneConstants.UPPER_LEADING_CORNER),
    UPTRAILING(ScrollPaneConstants.UPPER_TRAILING_CORNER);

    private final String    _p;
    public final String getPosition ()
    {
        return _p;
    }

    CornerPosition (String p)
    {
        _p = p;
    }

    public static final List<CornerPosition>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final CornerPosition fromString (final String name)
    {
        return CollectionsUtils.fromString(VALUES, name, false);
    }

    public static final CornerPosition fromPosition (final String p)
    {
        if ((null == p) || (p.length() <= 0))
            return null;

        for (final CornerPosition v : VALUES)
        {
            final String    vp=(null == v) ? null : v.getPosition();
            if (p.equalsIgnoreCase(vp))
                return v;
        }

        return null;
    }
}
