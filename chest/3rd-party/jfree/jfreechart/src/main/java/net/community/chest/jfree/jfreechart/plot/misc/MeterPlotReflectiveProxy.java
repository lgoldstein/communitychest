/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.misc;

import java.text.NumberFormat;

import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy;
import net.community.chest.text.NumberFormatReflectiveProxy;

import org.jfree.chart.plot.MeterPlot;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link MeterPlot}
 * @author Lyor G.
 * @since Feb 9, 2009 11:17:40 AM
 */
public class MeterPlotReflectiveProxy<P extends MeterPlot> extends PlotReflectiveProxy<P> {
    protected MeterPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public MeterPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    TICK_LABEL_FMT_ATTR="TickLabelFormat";
    public boolean isTickLabelFormatElement (Element elem, String tagName)
    {
        return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, TICK_LABEL_FMT_ATTR);
    }

    public XmlValueInstantiator<? extends NumberFormat>    getTickLabelFormatConverter (Element elem)
    {
        return (null == elem) ? null : NumberFormatReflectiveProxy.getNumberFormatConverter(elem);
    }

    public NumberFormat setTickLabelFormat (P src, Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends NumberFormat>    conv=getTickLabelFormatConverter(elem);
        final NumberFormat                                    fmt=conv.fromXml(elem);
        if (fmt != null)
            src.setTickLabelFormat(fmt);
        return fmt;
    }
    /*
     * @see net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy#fromXmlChild(org.jfree.chart.plot.Plot, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=(null == elem) ? null : elem.getTagName();
        if (isTickLabelFormatElement(elem, tagName))
        {
            setTickLabelFormat(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final MeterPlotReflectiveProxy<MeterPlot>    METERPLOT=
        new MeterPlotReflectiveProxy<MeterPlot>(MeterPlot.class, true);
}
