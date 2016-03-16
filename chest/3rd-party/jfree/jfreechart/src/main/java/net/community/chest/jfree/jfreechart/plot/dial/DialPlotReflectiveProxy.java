/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.dial;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.plot.dial.DialPlot;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link DialPlot}
 * @author Lyor G.
 * @since Feb 9, 2009 11:15:20 AM
 */
public class DialPlotReflectiveProxy<P extends DialPlot> extends PlotReflectiveProxy<P> {
    protected DialPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public DialPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    VIEW_ATTR="view";
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#handleUnknownAttribute(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    protected P handleUnknownAttribute (P src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
    {
        if (VIEW_ATTR.equalsIgnoreCase(name))
        {
            final List<String>    vl=StringUtil.splitString(value, ',');
            final int            numValues=(null == vl) ? 0 : vl.size();
            if (numValues < 4)
                throw new IllegalArgumentException("handleUnknownAttribute(" + name + ")[" + value + "] incomplete specification");

            final double[]    va=new double[numValues];
            for (int    vIndex=0; vIndex < numValues; vIndex++)
            {
                final String    vs=vl.get(vIndex);
                final Double    dv=DoubleValueStringConstructor.DEFAULT.newInstance(vs);
                if (null == dv)
                    throw new IllegalArgumentException("handleUnknownAttribute(" + name + ")[" + value + "] missing value #" + (vIndex + 1));

                final double    dd=dv.doubleValue();
                if (Double.isInfinite(dd) || Double.isNaN(dd))
                    throw new IllegalArgumentException("handleUnknownAttribute(" + name + ")[" + value + "] bad value #" + (vIndex + 1) + " (" + vs + ")");

                va[vIndex] = dd;
            }

            src.setView(va[0], va[1], va[2], va[3]);
            return src;
        }

        return super.handleUnknownAttribute(src, name, value, accsMap);
    }

    public static final DialPlotReflectiveProxy<DialPlot>    DIALPLOT=
            new DialPlotReflectiveProxy<DialPlot>(DialPlot.class, true);
}
