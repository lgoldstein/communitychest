package net.community.chest.awt.layout.gridbag;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Represents the various {@link GridBagConstraints} anchor values as
 * {@link Enum}-erated values - providing conversion utilities between them</P>
 *
 * @author Lyor G.
 * @since Aug 7, 2007 12:50:48 PM
 */
public enum GridBagAnchorType {
    NORTH(GridBagConstraints.NORTH, true),
    SOUTH(GridBagConstraints.SOUTH, true),
    WEST(GridBagConstraints.WEST, true),
    EAST(GridBagConstraints.EAST, true),
    NORTHWEST(GridBagConstraints.NORTHWEST, true),
    SOUTHWEST(GridBagConstraints.SOUTHWEST, true),
    NORTHEAST(GridBagConstraints.NORTHEAST, true),
    SOUTHEAST(GridBagConstraints.SOUTHEAST, true),

    PAGESTART(GridBagConstraints.PAGE_START, false),
    PAGEEND(GridBagConstraints.PAGE_END, false),
    LINESTART(GridBagConstraints.LINE_START, false),
    LINEEND(GridBagConstraints.LINE_END, false),
    FIRSTLINESTART(GridBagConstraints.FIRST_LINE_START, false),
    FIRSTLINEEND(GridBagConstraints.FIRST_LINE_END, false),
    LASTLINESTART(GridBagConstraints.LAST_LINE_START, false),
    LASTLINEEND(GridBagConstraints.LAST_LINE_END, false),

    CENTER(GridBagConstraints.CENTER, true);

    private final int    _anchorValue;
    public int getAnchorValue ()
    {
        return _anchorValue;
    }

    private final boolean    _absolute;
    public boolean isAbsolute ()
    {
        return _absolute;
    }

    GridBagAnchorType (final int anchorValue, final boolean absolute)
    {
        _anchorValue = anchorValue;
        _absolute = absolute;
    }

    public static final List<GridBagAnchorType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    private static List<GridBagAnchorType> getValuesByAbsoluteState (final boolean absVal)
    {
        List<GridBagAnchorType>    ret=new ArrayList<GridBagAnchorType>(VALUES.size());
        for (final GridBagAnchorType v : VALUES)
        {
            if ((v != null) && (v.isAbsolute() == absVal))
            {
                if (null == ret)
                    ret = new LinkedList<GridBagAnchorType>();
                ret.add(v);
            }
        }

        return ret;
    }

    private static List<GridBagAnchorType>    _absVals    /* =null */;
    public static synchronized List<GridBagAnchorType> getAbsoluteValues ()
    {
        if (null == _absVals)
            _absVals = getValuesByAbsoluteState(true);
        return _absVals;
    }

    private static List<GridBagAnchorType>    _relVals    /* =null */;
    public static synchronized List<GridBagAnchorType> getRelativeValues ()
    {
        if (null == _relVals)
            _relVals = getValuesByAbsoluteState(false);
        return _relVals;
    }

    public static GridBagAnchorType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static GridBagAnchorType fromAnchorValue (final int value)
    {
        for (final GridBagAnchorType v : VALUES)
        {
            if ((v != null) && (v.getAnchorValue() == value))
                return v;
        }

        return null;
    }
}
