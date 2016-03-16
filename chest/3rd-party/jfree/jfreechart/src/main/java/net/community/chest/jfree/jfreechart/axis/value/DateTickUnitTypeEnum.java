/*
 *
 */
package net.community.chest.jfree.jfreechart.axis.value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.axis.DateTickUnitType;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulates the {@link DateTickUnitType} units as an {@link Enum}</P>
 * @author Lyor G.
 * @since May 5, 2009 2:54:45 PM
 */
public enum DateTickUnitTypeEnum {
    YEAR(DateTickUnitType.YEAR),
    MONTH(DateTickUnitType.MONTH),
    DAY(DateTickUnitType.DAY),
    HOUR(DateTickUnitType.HOUR),
    MINUTE(DateTickUnitType.MINUTE),
    SECOND(DateTickUnitType.SECOND),
    MSEC(DateTickUnitType.MILLISECOND);

    private final DateTickUnitType    _u;
    public final DateTickUnitType getUnitType ()
    {
        return _u;
    }

    DateTickUnitTypeEnum (DateTickUnitType u)
    {
        _u = u;
    }

    public static final List<DateTickUnitTypeEnum>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final DateTickUnitTypeEnum fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final DateTickUnitTypeEnum fromUnitType (final DateTickUnitType u)
    {
        if (null == u)
            return null;

        for (final DateTickUnitTypeEnum v : VALUES)
        {
            if ((v != null) && u.equals(v.getUnitType()))
                return v;
        }

        return null;
    }
}
