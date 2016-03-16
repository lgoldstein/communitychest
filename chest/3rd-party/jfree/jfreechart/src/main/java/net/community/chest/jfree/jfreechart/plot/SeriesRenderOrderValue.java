/*
 *
 */
package net.community.chest.jfree.jfreechart.plot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.plot.SeriesRenderingOrder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 8, 2009 2:43:08 PM
 */
public enum SeriesRenderOrderValue {
    FORWARD(SeriesRenderingOrder.FORWARD),
    REVERSE(SeriesRenderingOrder.REVERSE);

    private final SeriesRenderingOrder    _o;
    public final SeriesRenderingOrder getOrder ()
    {
        return _o;
    }

    SeriesRenderOrderValue (SeriesRenderingOrder o)
    {
        _o = o;
    }

    public static final List<SeriesRenderOrderValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final SeriesRenderOrderValue fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final SeriesRenderOrderValue fromOrder (final SeriesRenderingOrder o)
    {
        if (null == o)
            return null;

        for (final SeriesRenderOrderValue  v : VALUES)
        {
            if ((v != null) && o.equals(v.getOrder()))
                return v;
        }

        return null;
    }
}
