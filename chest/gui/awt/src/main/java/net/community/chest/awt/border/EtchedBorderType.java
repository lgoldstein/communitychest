/*
 *
 */
package net.community.chest.awt.border;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.border.EtchedBorder;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to convert between <code>int</code> values and {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Oct 6, 2008 4:10:40 PM
 */
public enum EtchedBorderType {
    LOWERED(EtchedBorder.LOWERED),
    RAISED(EtchedBorder.RAISED);

    private final int    _type;
    public final int getType ()
    {
        return _type;
    }

    EtchedBorderType (final int t)
    {
        _type = t;
    }

    public static final List<EtchedBorderType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final EtchedBorderType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final EtchedBorderType fromType (final int t)
    {
        for (final EtchedBorderType v : VALUES)
        {
            if ((v != null) && (v.getType() == t))
                return v;
        }

        return null;
    }

}
