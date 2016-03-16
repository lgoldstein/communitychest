/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.misc;

import java.util.Date;
import java.util.NoSuchElementException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.jfree.jfreechart.axis.value.DateTickUnitConverter;
import net.community.chest.jfree.jfreechart.axis.value.NumberTickUnitConverter;
import net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy;

import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PolarPlot;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <P> The reflected {@link PolarPlot} type
 * @author Lyor G.
 * @since May 26, 2009 12:46:42 PM
 */
public class PolarPlotReflectiveProxy<P extends PolarPlot> extends PlotReflectiveProxy<P> {
    protected PolarPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public PolarPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    AXIS_ATTR="Axis";
    public boolean isAxisElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, AXIS_ATTR);
    }

    public ValueAxis setAxis (final P src, final Element elem) throws Exception
    {
        return setAxis(src, AXIS_ATTR, ValueAxis.class, elem);
    }

    public static final String    TICK_UNIT_ELEM_NAME="angleTickUnit";
    public boolean isAngleTickUnitElement (Element elem, String tagName)
    {
        return isMatchingElement(elem, tagName, TICK_UNIT_ELEM_NAME);
    }

    public XmlValueInstantiator<? extends TickUnit> getAngleTickUnitConverter (Element elem)
    {
        final String    unitType=(null == elem) ? null : elem.getAttribute(CLASS_ATTR);
        if ((null == unitType) || (unitType.length() <= 0))
            return null;

        if (Number.class.getSimpleName().equalsIgnoreCase(unitType))
            return NumberTickUnitConverter.DEFAULT;
        else if (Date.class.getSimpleName().equalsIgnoreCase(unitType))
            return DateTickUnitConverter.DEFAULT;

        throw new NoSuchElementException("getAngleTickUnitConverter(" + DOMUtils.toString(elem) + ") unknown unit type: " + unitType);
    }

    public TickUnit setAngleTickUnit (P src, Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends TickUnit>    p=getAngleTickUnitConverter(elem);
        final TickUnit                                    u=(null == p) ? null : p.fromXml(elem);
        if (u != null)
            src.setAngleTickUnit(u);

        return u;
    }
    /*
     * @see net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy#fromXmlChild(org.jfree.chart.plot.Plot, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=(null == elem) ? null : elem.getTagName();
        if (isAxisElement(elem, tagName))
        {
            setAxis(src, elem);
            return src;
        }
        else if (isAngleTickUnitElement(elem, tagName))
        {
            setAngleTickUnit(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final PolarPlotReflectiveProxy<PolarPlot>    POLAR=
        new PolarPlotReflectiveProxy<PolarPlot>(PolarPlot.class, true);
}
