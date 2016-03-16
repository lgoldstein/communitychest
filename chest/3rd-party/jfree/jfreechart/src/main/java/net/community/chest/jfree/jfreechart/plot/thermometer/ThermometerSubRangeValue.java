/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.thermometer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.plot.ThermometerPlot;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>Encapsulates as {@link Enum} the {@link ThermometerPlot} sub-range values</P>
 * @author Lyor G.
 * @since Jun 21, 2010 2:50:32 PM
 */
public enum ThermometerSubRangeValue {
    NORMAL(ThermometerPlot.NORMAL, 'C'),
    WARNING(ThermometerPlot.WARNING, 'W'),
    CRITICAL(ThermometerPlot.CRITICAL, 'C');

    private final int    _r;
    public final int getRangeValue ()
    {
        return _r;
    }

    public final char    _c;
    public final char getRangeChar ()
    {
        return _c;
    }

    ThermometerSubRangeValue (final int r, final char c)
    {
        _r = r;
        _c = c;
    }

    public static final List<ThermometerSubRangeValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final ThermometerSubRangeValue fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final ThermometerSubRangeValue fromRangeValue (final int u)
    {
        for (final ThermometerSubRangeValue v : VALUES)
        {
            if ((v != null) && (v.getRangeValue() == u))
                return v;
        }

        return null;
    }

    public static final ThermometerSubRangeValue fromRangeChar (final char c)
    {
        final char    vc=Character.toUpperCase(c);
        for (final ThermometerSubRangeValue v : VALUES)
        {
            if ((v != null) && (v.getRangeChar() == vc))
                return v;
        }

        return null;
    }
}
