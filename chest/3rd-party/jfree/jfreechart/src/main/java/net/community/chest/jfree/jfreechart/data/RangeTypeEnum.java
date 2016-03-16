/*
 *
 */
package net.community.chest.jfree.jfreechart.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.data.RangeType;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 27, 2009 2:31:55 PM
 */
public enum RangeTypeEnum {
    FULL(RangeType.FULL),
    POSITIVE(RangeType.POSITIVE),
    NEGATIVE(RangeType.NEGATIVE);

    private final RangeType    _rangeType;
    public final RangeType getRangeType ()
    {
        return _rangeType;
    }

    RangeTypeEnum (RangeType rt)
    {
        _rangeType = rt;
    }

    public static final List<RangeTypeEnum>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final RangeTypeEnum fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final RangeTypeEnum fromRangeType (final RangeType rt)
    {
        if (null == rt)
            return null;

        for (final RangeTypeEnum  v : VALUES)
        {
            if ((v != null) && rt.equals(v.getRangeType()))
                return v;
        }

        return null;
    }
}
