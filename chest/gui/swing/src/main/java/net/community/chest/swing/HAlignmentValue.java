package net.community.chest.swing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingConstants;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates horizontal alignment values</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 11:09:22 AM
 */
public enum HAlignmentValue {
    LEFT(SwingConstants.LEFT),
    CENTER(SwingConstants.CENTER),
    RIGHT(SwingConstants.RIGHT),
    LEADING(SwingConstants.LEADING),
    TRAILING(SwingConstants.TRAILING);

    private final int    _alignValue;
    public final int getAlignmentValue ()
    {
        return _alignValue;
    }

    HAlignmentValue (final int alignValue)
    {
        _alignValue = alignValue;
    }

    public static final List<HAlignmentValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final HAlignmentValue fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final HAlignmentValue fromAlignmentValue (final int av)
    {
        for (final HAlignmentValue v : VALUES)
        {
            if ((v != null) && (v.getAlignmentValue() == av))
                return v;
        }

        return null;
    }
}
