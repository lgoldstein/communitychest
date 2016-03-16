/*
 *
 */
package net.community.chest.jfree.jfreechart.plot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.dom.DOMUtils;
import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.plot.PlotOrientation;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate the {@link PlotOrientation} values as an {@link Enum}
 * @author Lyor G.
 * @since Feb 5, 2009 3:07:10 PM
 */
public enum PlotOrientationValue {
    HORIZONTAL(PlotOrientation.HORIZONTAL),
    VERTICAL(PlotOrientation.VERTICAL);

    private final PlotOrientation    _o;
    public final PlotOrientation getOrientation ()
    {
        return _o;
    }

    PlotOrientationValue (PlotOrientation o)
    {
        _o = o;
    }

    public static final List<PlotOrientationValue>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final PlotOrientationValue fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final PlotOrientationValue fromOrientation (final PlotOrientation o)
    {
        if (null == o)
            return null;

        for (final PlotOrientationValue  v : VALUES)
        {
            if ((v != null) && o.equals(v.getOrientation()))
                return v;
        }

        return null;
    }

    public static final String    ORIENTATION_ATTR="orientation";
    public static final Attr getOrientationAttribute (Element elem)
    {
        return DOMUtils.findFirstAttribute(elem, false, ORIENTATION_ATTR);
    }
}
