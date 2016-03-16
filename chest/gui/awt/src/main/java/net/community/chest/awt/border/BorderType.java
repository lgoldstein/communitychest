/*
 *
 */
package net.community.chest.awt.border;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 1, 2009 10:34:21 AM
 */
public enum BorderType {
    BEVEL(BevelBorder.class),
    COMPOUND(CompoundBorder.class),
    ETCHED(EtchedBorder.class),
    LINE(LineBorder.class),
    TITLED(TitledBorder.class),
    EMPTY(EmptyBorder.class),
    MATTE(MatteBorder.class);

    private final Class<? extends Border>    _bc;
    public final Class<? extends Border> getBorderClass ()
    {
        return _bc;
    }

    BorderType (Class<? extends Border> bc)
    {
        _bc = bc;
    }

    public static final List<BorderType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final BorderType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final BorderType fromClassName (final String n)
    {
        if ((null == n) || (n.length() <= 0))
            return null;

        for (final BorderType v : VALUES)
        {
            final Class<?>    bc=(null == v) ? null : v.getBorderClass();
            final String    sn=(null == bc) ? null : bc.getSimpleName();
            if (n.equalsIgnoreCase(sn))
                return v;
        }

        return null;
    }

    public static final BorderType fromClass (Class<?> c, boolean exactMatch)
    {
        if ((null == c) || (!Border.class.isAssignableFrom(c)))
            return null;

        for (final BorderType v : VALUES)
        {
            final Class<?>    bc=(null == v) ? null : v.getBorderClass();
            if ((null == bc) || (!bc.isAssignableFrom(c)))
                continue;
            if ((!exactMatch) || c.isAssignableFrom(bc))
                return v;
        }

        return null;
    }

    public static final BorderType fromObject (Object o, boolean exactMatch)
    {
        return (null == o) ? null : fromClass(o.getClass(), exactMatch);
    }
}
