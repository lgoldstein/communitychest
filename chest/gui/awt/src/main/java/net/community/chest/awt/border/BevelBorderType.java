/*
 *
 */
package net.community.chest.awt.border;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.border.BevelBorder;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to convert between <code>int</code> values and {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Oct 6, 2008 4:01:38 PM
 */
public enum BevelBorderType {
    LOWERED(BevelBorder.LOWERED),
    RAISED(BevelBorder.RAISED);

    private final int    _type;
    public final int getType ()
    {
        return _type;
    }

    BevelBorderType (final int t)
    {
        _type = t;
    }

    public static final List<BevelBorderType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final BevelBorderType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final BevelBorderType fromType (final int t)
    {
        for (final BevelBorderType v : VALUES)
        {
            if ((v != null) && (v.getType() == t))
                return v;
        }

        return null;
    }
}
