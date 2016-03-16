/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.compass;

import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy;
import net.community.chest.util.map.MapEntryImpl;

import org.jfree.chart.plot.CompassPlot;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @param <P> The reflected {@link CompassPlot} type
 * @since Feb 16, 2009 1:55:27 PM
 */
public class CompassPlotReflectiveProxy<P extends CompassPlot> extends PlotReflectiveProxy<P> {
    protected CompassPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public CompassPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        super(objClass, false);
    }

    public static final String    LABEL_TYPE_ATTR="LabelType",
                                SERIES_NEEDLE_ATTR="SeriesNeedle";
    /*
     * @see net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if (LABEL_TYPE_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) CompassPlotLabelTypeValueStringInstantiator.DEFAULT;
        else if (SERIES_NEEDLE_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) NeedleTypeValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }

    public boolean isSeriesNeedleElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, SERIES_NEEDLE_ATTR);
    }

    public Map.Entry<Integer,NeedleType> setSeriesNeedle (P src, Element elem) throws Exception
    {
        final String        idxStr=elem.getAttribute(NAME_ATTR),
                            typStr=elem.getAttribute(CLASS_ATTR);
        final int            idx=
            ((null == idxStr) || (idxStr.length() <= 0)) ? (-1) : Integer.parseInt(idxStr);
        final NeedleType    typ=NeedleType.fromString(typStr);
        if ((idx < 0) || (null == typ))
            throw new NoSuchElementException("setSeriesNeedle(" + DOMUtils.toString(elem) + ") unknown/missing data");

        src.setSeriesNeedle(idx, typ.getOrderValue());
        return new MapEntryImpl<Integer,NeedleType>(Integer.valueOf(idx), typ);
    }
    /*
     * @see net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy#fromXmlChild(org.jfree.chart.plot.Plot, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=(null == elem) ? null : elem.getTagName();
        if (isSeriesNeedleElement(elem, tagName))
        {
            setSeriesNeedle(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final CompassPlotReflectiveProxy<CompassPlot>    COMPASSPLOT=
                new CompassPlotReflectiveProxy<CompassPlot>(CompassPlot.class, true);
}
