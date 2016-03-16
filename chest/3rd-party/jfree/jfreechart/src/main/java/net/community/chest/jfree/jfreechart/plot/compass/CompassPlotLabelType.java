/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.compass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.plot.CompassPlot;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate <code>int</code>-s as {@link Enum}-s</P>
 * @author Lyor G.
 * @since Feb 16, 2009 1:57:38 PM
 */
public enum CompassPlotLabelType {
    NONE(CompassPlot.NO_LABELS),
    VALUE(CompassPlot.VALUE_LABELS);

    private final int    _type;
    public final int getType ()
    {
        return _type;
    }

    CompassPlotLabelType (int t)
    {
        _type = t;
    }

    public static final List<CompassPlotLabelType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final CompassPlotLabelType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final CompassPlotLabelType fromType (final int t)
    {
        for (final CompassPlotLabelType v : VALUES)
        {
            if ((v != null) && (v.getType() == t))
                return v;
        }

        return null;
    }
}
