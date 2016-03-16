/*
 *
 */
package net.community.chest.jfree.jfreechart.plot;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy;
import net.community.chest.jfree.jfreechart.axis.AxisReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.AxisSpaceReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.category.CategoryPlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.category.CombinedDomainCategoryPlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.category.CombinedRangeCategoryPlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.compass.CompassPlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.dial.DialPlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.misc.MeterPlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.misc.PolarPlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.pie.MultiplePiePlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.pie.PiePlot3DReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.pie.PiePlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.pie.RingPlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.xy.CombinedDomainXYPlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.xy.CombinedRangeXYPlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.plot.xy.XYPlotReflectiveProxy;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.data.general.DatasetGroup;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> Type of reflected {@link Plot}
 * @author Lyor G.
 * @since Feb 1, 2009 2:37:31 PM
 */
public class PlotReflectiveProxy<P extends Plot> extends ChartReflectiveAttributesProxy<P> {
    protected PlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final String    GROUP_ATTR="datasetGroup",
                                BG_IMG_ATTR="backgroundImage";
    /* NOTE: contains support for various sub-classes
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected P updateObjectAttribute (P src, String name, String value, Method setter) throws Exception
    {
        if (GROUP_ATTR.equalsIgnoreCase(name))
        {
            setter.invoke(src, new DatasetGroup(value));
            return src;
        }
        else if (BG_IMG_ATTR.equalsIgnoreCase(name))
            return updateObjectResourceAttribute(src, name,value, setter);

        return super.updateObjectAttribute(src, name, value, setter);
    }

    public static final String    PLOT_ELEM_NAME=Plot.class.getSimpleName().toLowerCase();
    public static final boolean isPlotElement (Element elem, String tagName)
    {
        return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, PLOT_ELEM_NAME);
    }

    public static final boolean isPlotElement (Element elem)
    {
        return (null == elem) ? false : isPlotElement(elem, elem.getTagName());
    }

    private static Map<String,PlotReflectiveProxy<?>>    _plotsMap    /* =null */;
    public static final synchronized Map<String,PlotReflectiveProxy<?>> getPlotConvertersMap ()
    {
        if (null == _plotsMap)
        {
            _plotsMap = new TreeMap<String,PlotReflectiveProxy<?>>(String.CASE_INSENSITIVE_ORDER);

            final PlotReflectiveProxy<?>[]    convs={
                    PiePlotReflectiveProxy.PIEPLOT,
                    PiePlot3DReflectiveProxy.PIEPLOT3D,
                    RingPlotReflectiveProxy.RINGPLOT,
                    MultiplePiePlotReflectiveProxy.MULTIPIE,

                    CategoryPlotReflectiveProxy.CATPLOT,
                    CombinedDomainCategoryPlotReflectiveProxy.COMBDOMAINCATPLOT,
                    CombinedRangeCategoryPlotReflectiveProxy.COMBRNGCATPLOT,

                    XYPlotReflectiveProxy.XYPLOT,
                    CombinedDomainXYPlotReflectiveProxy.COMBDOMXYPLOT,
                    CombinedRangeXYPlotReflectiveProxy.COMBRNGXYPLOT,

                    DialPlotReflectiveProxy.DIALPLOT,
                    MeterPlotReflectiveProxy.METERPLOT,
                    CompassPlotReflectiveProxy.COMPASSPLOT,
                    PolarPlotReflectiveProxy.POLAR
                };

            for (final PlotReflectiveProxy<?> p : convs)
            {
                final Class<?>    pc=(null == p) ? null : p.getValuesClass();
                final String    cn=(null == pc) ? null : pc.getSimpleName();
                if ((null == cn) || (cn.length() <= 0))
                    continue;

                _plotsMap.put(cn, p);
            }
        }

        return _plotsMap;
    }

    public static final PlotReflectiveProxy<?> getPlotConverter (final String cn)
    {
        if ((null == cn) || (cn.length() <= 0))
            return null;

        final Map<String,? extends PlotReflectiveProxy<?>>    pm=getPlotConvertersMap();
        if ((null == pm) || (pm.size() <= 0))
            return null;

        synchronized(pm)
        {
            return pm.get(cn);
        }
    }

    public static final PlotReflectiveProxy<?> getPlotConverter (final Class<?> c)
    {
        return (null == c) ? null : getPlotConverter(c.getSimpleName());
    }

    public static final PlotReflectiveProxy<?> getPlotConverter (final Plot p)
    {
        return (null == p) ? null : getPlotConverter(p.getClass());
    }

    public static final PlotReflectiveProxy<?> getPlotConverter (final Element elem)
    {
        return (null == elem) ? null : getPlotConverter(elem.getAttribute(CLASS_ATTR));
    }

    protected <A extends Axis> A setAxis (final P src, final Method setter, final Class<A> ac, final Element elem) throws Exception
    {
        final AxisReflectiveProxy<? extends Axis>    proxy=AxisReflectiveProxy.getAxisConverter(elem);
        final Axis                                    a=proxy.fromXml(elem);
        if (a != null)
        {
            setter.invoke(src, a);
            return ac.cast(a);
        }

        return null;
    }

    protected <A extends Axis> A setAxis (final P src, final String aName, final Class<A> ac, final Element elem) throws Exception
    {
        final Map<String,? extends Method>    sm=
            ((null == aName) || (aName.length() <= 0)) ? null : getSettersMap();
        final Method                        setter=
            ((null == sm) || (sm.size() <= 0)) ? null : sm.get(aName);
        return setAxis(src, setter, ac, elem);
    }

    public XmlValueInstantiator<? extends AxisSpace> getAxisSpaceConverter (Element elem)
    {
        return (null == elem) ? null : AxisSpaceReflectiveProxy.AXISSPACE;
    }

    protected AxisSpace setAxisSpace (P src, Element elem, String aName) throws Exception
    {
        final XmlValueInstantiator<? extends AxisSpace>    proxy=getAxisSpaceConverter(elem);
        final AxisSpace                                    spc=proxy.fromXml(elem);
        if (spc != null)
        {
            final Map<String,? extends Method>    sm=getSettersMap();
            final Method                        m=(null == sm) ? null : sm.get(aName);
            if (m == null)
                throw new NoSuchMethodException("No setter for " + aName);
            m.invoke(src, spc);
        }

        return spc;
    }

    public static final String    DOMAIN_AXIS_ATTR="DomainAxis";
    public boolean isDomainAxisElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, DOMAIN_AXIS_ATTR);
    }

    public CategoryAxis setDomainAxis (P src, Element elem) throws Exception
    {
        return setAxis(src, DOMAIN_AXIS_ATTR, CategoryAxis.class, elem);
    }

    public static final String    RANGE_AXIS_ATTR="RangeAxis";
    public boolean isRangeAxisElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, RANGE_AXIS_ATTR);
    }

    public ValueAxis setRangeAxis (P src, Element elem) throws Exception
    {
        return setAxis(src, RANGE_AXIS_ATTR, ValueAxis.class, elem);
    }

    public static final String    FIXED_DOMAIN_AXIS_SPACE_ATTR="FixedDomainAxisSpace";
    public boolean isFixedDomainAxisSpace (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, FIXED_DOMAIN_AXIS_SPACE_ATTR);
    }

    public AxisSpace setFixedDomainAxisSpace (P src, Element elem) throws Exception
    {
        return setAxisSpace(src, elem, FIXED_DOMAIN_AXIS_SPACE_ATTR);
    }

    public static final String    FIXED_RANGE_AXIS_SPACE="FixedRangeAxisSpace";
    public boolean isFixedRangeAxisSpace (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, FIXED_RANGE_AXIS_SPACE);
    }

    public AxisSpace setFixedRangeAxisSpace (P src, Element elem) throws Exception
    {
        return setAxisSpace(src, elem, FIXED_RANGE_AXIS_SPACE);
    }

    public static final String    FIXED_RANGE_FOR_SUBPLOTS_ATTR="FixedRangeAxisSpaceForSubplots";
    public boolean isFixedRangeAxisSpaceForSubplots (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, FIXED_RANGE_FOR_SUBPLOTS_ATTR);
    }

    public AxisSpace setFixedRangeAxisSpaceForSubplots (P src, Element elem) throws Exception
    {
        return setAxisSpace(src, elem, FIXED_RANGE_FOR_SUBPLOTS_ATTR);
    }

    public static final String    FIXED_DOMAIN_FOR_SUBPLOTS_ATTR="FixedDomainAxisSpaceForSubplots";
    public boolean isFixedDomainAxisSpaceForSubplots (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, FIXED_DOMAIN_FOR_SUBPLOTS_ATTR);
    }

    public AxisSpace setFixedDomainAxisSpaceForSubplots (P src, Element elem) throws Exception
    {
        return setAxisSpace(src, elem, FIXED_DOMAIN_FOR_SUBPLOTS_ATTR);
    }
    /* NOTE: contains support for various sub-classes
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=(null == elem) ? null : elem.getTagName();
        if (isDomainAxisElement(elem, tagName))
        {
            setDomainAxis(src, elem);
            return src;
        }
        else if (isRangeAxisElement(elem, tagName))
        {
            setRangeAxis(src, elem);
            return src;
        }
        else if (isFixedDomainAxisSpace(elem, tagName))
        {
            setFixedDomainAxisSpace(src, elem);
            return src;
        }
        else if (isFixedRangeAxisSpace(elem, tagName))
        {
            setFixedRangeAxisSpace(src, elem);
            return src;
        }
        else if (isFixedRangeAxisSpaceForSubplots(elem, tagName))
        {
            setFixedRangeAxisSpaceForSubplots(src, elem);
            return src;
        }
        else if (isFixedDomainAxisSpaceForSubplots(elem, tagName))
        {
            setFixedDomainAxisSpaceForSubplots(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }
}
