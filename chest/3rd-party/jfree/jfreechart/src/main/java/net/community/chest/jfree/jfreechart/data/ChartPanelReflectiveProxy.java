/*
 *
 */
package net.community.chest.jfree.jfreechart.data;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.JPopupMenu;

import net.community.chest.awt.menu.MenuReflectiveProxy;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.jfree.jfreechart.ChartColorValueInstantiator;
import net.community.chest.jfree.jfreechart.chart.JFreeChartReflectiveProxy;
import net.community.chest.swing.component.menu.JPopupMenuReflectiveProxy;
import net.community.chest.swing.component.panel.JPanelReflectiveProxy;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link ChartPanel} instance
 * @author Lyor G.
 * @since Jan 27, 2009 3:07:34 PM
 */
public class ChartPanelReflectiveProxy<P extends ChartPanel> extends JPanelReflectiveProxy<P> {
    protected ChartPanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if (Color.class.isAssignableFrom(type))
            return (ValueStringInstantiator<C>) ChartColorValueInstantiator.CHARTCOLOR;
        return super.resolveAttributeInstantiator(name, type);
    }

    public ChartPanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    private Map<String,String>    _defValsMap;
    public synchronized Map<String,String> getDefaultValuesMap ()
    {
        if (null == _defValsMap)
        {
            _defValsMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
            final String[]    defPairs={
                    "minimumDrawWidth", String.valueOf(ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH),
                    "maximumDrawWidth", String.valueOf(ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH),
                    "minimumDrawHeight", String.valueOf(ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT),
                    "maximumDrawHeight", String.valueOf(ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT),
                    "preferredSize", String.valueOf(ChartPanel.DEFAULT_WIDTH) + "," + String.valueOf(ChartPanel.DEFAULT_HEIGHT)
                };
            for (int    pIndex=0; pIndex < defPairs.length; pIndex += 2)
            {
                final String    k=defPairs[pIndex],
                                v=defPairs[pIndex+1],
                                prev=_defValsMap.put(k, v);
                if ((prev != null) && (prev.length() > 0))
                    throw new IllegalStateException("getDefaultValuesMap(" + k + ") duplicate value(s): " + v + "/" + prev);
            }
        }

        return _defValsMap;
    }
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected P updateObjectAttribute (P src, String name, String orgValue, Method setter) throws Exception
    {
        String    value=orgValue;
        if ("default".equalsIgnoreCase(value))
        {
            final Map<String,String>    dm=getDefaultValuesMap();
            value = ((null == dm) || (dm.size() <= 0)) ? null : dm.get(name);
            if ((null == value) || (value.length() <= 0))
                throw new NoSuchElementException("updateObjectAttribute(" + name + ") no default found");
        }

        return super.updateObjectAttribute(src, name, value, setter);
    }

    public XmlProxyConvertible<? extends JPopupMenu> getPopupMenuConverter (Element elem) throws Exception
    {
        return (null == elem) ? null : JPopupMenuReflectiveProxy.POPUPMENU;
    }

    public JPopupMenu setPopupMenu (P src, Element elem) throws Exception
    {
        final XmlProxyConvertible<? extends JPopupMenu>    proxy=getPopupMenuConverter(elem);
        @SuppressWarnings("unchecked")
        final JPopupMenu                                org=src.getPopupMenu(),
                                                        mnu=
                (null == org) ? proxy.fromXml(elem) : ((XmlProxyConvertible<JPopupMenu>) proxy).fromXml(org, elem);
        if (mnu != null)
        {
            if (null == org)
                src.setPopupMenu(mnu);
            else if (org != mnu)
                throw new IllegalStateException("setPopupMenu(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
        }

        return mnu;
    }

    public boolean isPopupMenuElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, MenuReflectiveProxy.MENU_ELEMNAME);
    }

    public XmlProxyConvertible<? extends JFreeChart> getChartConverter (Element elem)
    {
        return (null == elem) ? null : JFreeChartReflectiveProxy.CHART;
    }

    public JFreeChart setChart (P src, Element elem) throws Exception
    {
        final XmlProxyConvertible<? extends JFreeChart>    conv=getChartConverter(elem);
        @SuppressWarnings("unchecked")
        final JFreeChart                                orgChart=src.getChart(),
                                                        c=
            (null == orgChart) ? conv.fromXml(elem) : ((XmlProxyConvertible<JFreeChart>) conv).fromXml(orgChart, elem);
        if (c != null)
        {
            if (null == orgChart)
                src.setChart(c);
            else if (orgChart != c)
                throw new IllegalStateException("setChart(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
        }

        return c;
    }

    public boolean isChartElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, JFreeChartReflectiveProxy.CHART_ELEM_NAME);
    }
    /*
     * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isPopupMenuElement(elem, tagName))
        {
            setPopupMenu(src, elem);
            return src;
        }
        else if (isChartElement(elem, tagName))
        {
            setChart(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }
    // NOTE !!! attempting to call "fromXml(Element)" will cause exception since no valid constructor available
    public static final ChartPanelReflectiveProxy<ChartPanel>    CHRTPNL=
            new ChartPanelReflectiveProxy<ChartPanel>(ChartPanel.class, true);
}
