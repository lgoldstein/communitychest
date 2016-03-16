package net.community.chest.awt.layout.gridbag;

import java.awt.GridBagConstraints;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Represents the various {@link GridBagConstraints} fill options as
 * {@link Enum}-eration objects + some conversion utilities
 * @author Lyor G.
 * @since Aug 7, 2007 1:07:57 PM
 */
public enum GridBagFillType {
    NONE(GridBagConstraints.NONE),
    HORIZONTAL(GridBagConstraints.HORIZONTAL),
    VERTICAL(GridBagConstraints.VERTICAL),
    BOTH(GridBagConstraints.BOTH);

    private final int    _fillValue;
    public int getFillValue ()
    {
        return _fillValue;
    }

    GridBagFillType (final int fillValue)
    {
        _fillValue = fillValue;
    }

    public static final List<GridBagFillType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static GridBagFillType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static GridBagFillType fromFillValue (final int fillValue)
    {
        for (final GridBagFillType v : VALUES)
        {
            if ((v != null) && (v.getFillValue() == fillValue))
                return v;
        }

        return null;
    }
}
