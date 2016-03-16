/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.thermometer;

import java.awt.Paint;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.jfree.jfreechart.data.RangeValueStringInstantiator;
import net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy;
import net.community.chest.text.NumberFormatReflectiveProxy;

import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.data.Range;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @param <P>
 * @since Feb 9, 2009 11:59:26 AM
 */
public class ThermometerPlotReflectiveProxy<P extends ThermometerPlot> extends PlotReflectiveProxy<P> {
    protected ThermometerPlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public ThermometerPlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    RANGE_ATTR=Range.class.getSimpleName(),
                                VALUE_FMT_ATTR="ValueFormat",
                                UNITS_ATTR="Units",
                                AXIS_LOCATION_ATTR="AxisLocation",
                                VALUE_LOCATION_ATTR="ValueLocation";
    /*
     * @see net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (final String aName, final Class<C> aType) throws Exception
    {
        if (UNITS_ATTR.equalsIgnoreCase(aName))
            return (ValueStringInstantiator<C>) ThermometerUnitValueStringInstantiator.DEFAULT;
        else if (AXIS_LOCATION_ATTR.equalsIgnoreCase(aName)
              || VALUE_LOCATION_ATTR.equalsIgnoreCase(aName))
            return (ValueStringInstantiator<C>) ThermometerLocationValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(aName, aType);
    }
    /*
     * @see net.community.chest.dom.proxy.ReflectiveAttributesProxy#handleUnknownAttribute(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    protected P handleUnknownAttribute (P src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
    {
        if (RANGE_ATTR.equalsIgnoreCase(name))
        {
            final Range    r=RangeValueStringInstantiator.DEFAULT.newInstance(value);
            src.setRange(r.getLength(), r.getUpperBound());
            return src;
        }

        return super.handleUnknownAttribute(src, name, value, accsMap);
    }

    public boolean isValueFormatElement (Element elem, String tagName)
    {
        return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, VALUE_FMT_ATTR);
    }

    public XmlValueInstantiator<? extends NumberFormat>    getValueFormatConverter (Element elem)
    {
        return (null == elem) ? null : NumberFormatReflectiveProxy.getNumberFormatConverter(elem);
    }

    public NumberFormat setValueFormat (P src, Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends NumberFormat>    conv=getValueFormatConverter(elem);
        final NumberFormat                                    fmt=conv.fromXml(elem);
        if (fmt != null)
            src.setValueFormat(fmt);
        return fmt;
    }

    public static final String    SUBRANGE_INFO_ATTR="SubrangeInfo";
    public boolean isSubRangeInfoElement (Element elem, String tagName)
    {
        return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, SUBRANGE_INFO_ATTR);
    }

    public Paint setSubRangePaint (P src, ThermometerSubRangeValue range, Element elem)
        throws Exception
    {
        final String    color=(null == elem) ? null : elem.getAttribute("paint");
        if ((null == color) || (color.length() <= 0))
            return null;

        final ValueStringInstantiator<?>    vsi=
            getAttributeInstantiator(src, "SubrangePaint", Paint.class);
        final Object                        o=
            (null == vsi) ? null : vsi.newInstance(color);
        if (!(o instanceof Paint))
            throw new NoSuchElementException("setSubRangeInfo(" + DOMUtils.toString(elem) + ") no paint value found: " + color);

        src.setSubrangePaint(range.getRangeValue(), (Paint) o);
        return (Paint) o;
    }

    private static final Double getDouble (Element elem, String aName) throws Exception
    {
        final String    aValue=(null == elem) ? null : elem.getAttribute(aName);
        if ((null == aValue) || (aValue.length() <= 0))
            return null;

        return DoubleValueStringConstructor.DEFAULT.newInstance(aValue);
    }

    public static final String    RANGE_LO_ATTR="rangeLow", RANGE_HI_ATTR="rangeHigh",
                                DISP_LO_ATTR="displayLow", DISP_HI_ATTR="displayHigh";
    public static final String[]    RANGE_VALS={
            RANGE_LO_ATTR, RANGE_HI_ATTR,
            DISP_LO_ATTR, DISP_HI_ATTR
        };
    public static final Map<String,Double> getRangeValues (Element elem) throws Exception
    {
        Map<String,Double>    ret=null;
        for (final String aName : RANGE_VALS)
        {
            final Double    aValue=getDouble(elem, aName);
            if (null == aValue)
                continue;

            if (null == ret)
                ret = new TreeMap<String,Double>(String.CASE_INSENSITIVE_ORDER);

            final Double    prev=ret.put(aName, aValue);
            if (prev != null)
                throw new IllegalStateException("getRangeValues(" + aName + ") duplicate values ("
                                              + prev + "/" + aValue + ") in " + DOMUtils.toString(elem));
        }

        return ret;
    }

    public Map<String,Double> setSubRangeInfo (P src, ThermometerSubRangeValue range, Element elem)
        throws Exception
    {
        final Map<String,Double>    rangeVals=getRangeValues(elem);
        if ((null == rangeVals) || (rangeVals.size() <= 0))
            return rangeVals;

        final Double[]    vals={
                rangeVals.get(RANGE_LO_ATTR), rangeVals.get(RANGE_HI_ATTR),
                rangeVals.get(DISP_LO_ATTR), rangeVals.get(DISP_HI_ATTR)
            };
        if ((null == vals[0]) || (null == vals[1]))
            throw new IllegalArgumentException("setSubRangeInfo(" + range + ") missing range value in " + DOMUtils.toString(elem));

        if (null == vals[2])
        {
            if (vals[3] != null)
                throw new IllegalArgumentException("setSubRangeInfo(" + range + ") "
                                                  + "extraneous " + DISP_HI_ATTR + " value in " + DOMUtils.toString(elem));

            src.setSubrangeInfo(range.getRangeValue(), vals[0].doubleValue(), vals[1].doubleValue());
        }
        else
        {
            if (null == vals[3])
                throw new IllegalArgumentException("setSubRangeInfo(" + range + ") "
                          + "missing " + DISP_HI_ATTR + " value in " + DOMUtils.toString(elem));

            src.setSubrangeInfo(range.getRangeValue(),
                                vals[0].doubleValue(), vals[1].doubleValue(),
                                vals[2].doubleValue(), vals[3].doubleValue());
        }

        return rangeVals;
    }

    public void setSubRangeInfo (P src, Element elem) throws Exception
    {
        final String                rangeVal=elem.getAttribute("range");
        ThermometerSubRangeValue    range=ThermometerSubRangeValue.fromString(rangeVal);
        if ((null == range) && (rangeVal != null) && (rangeVal.length() == 1))
            range = ThermometerSubRangeValue.fromRangeChar(rangeVal.charAt(0));
        if (null == range)
            throw new NoSuchElementException("setSubRangeInfo(" + DOMUtils.toString(elem) + ") no range value found: " + rangeVal);

        setSubRangeInfo(src, range, elem);
        setSubRangePaint(src, range, elem);
    }
    /*
     * @see net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy#fromXmlChild(org.jfree.chart.plot.Plot, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=(null == elem) ? null : elem.getTagName();
        if (isValueFormatElement(elem, tagName))
        {
            setValueFormat(src, elem);
            return src;
        }
        else if (isSubRangeInfoElement(elem, tagName))
        {
            setSubRangeInfo(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final ThermometerPlotReflectiveProxy<ThermometerPlot>    THERMPLOT=
        new ThermometerPlotReflectiveProxy<ThermometerPlot>(ThermometerPlot.class, true);
}
