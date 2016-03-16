/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.dial;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.plot.DialShape;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 9, 2009 11:19:33 AM
 */
public enum DialShapeValue {
    CIRCLE(DialShape.CIRCLE),
    CHORD(DialShape.CHORD),
    PIE(DialShape.PIE);

    private final DialShape    _s;
    public final DialShape getShape ()
    {
        return _s;
    }

    DialShapeValue (DialShape s)
    {
        _s = s;
    }

    public static final List<DialShapeValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final DialShapeValue fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final DialShapeValue fromShape (final DialShape s)
    {
        if (null == s)
            return null;

        for (final DialShapeValue  v : VALUES)
        {
            if ((v != null) && s.equals(v.getShape()))
                return v;
        }

        return null;
    }

}
