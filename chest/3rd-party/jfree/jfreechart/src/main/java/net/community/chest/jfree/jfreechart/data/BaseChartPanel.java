/*
 *
 */
package net.community.chest.jfree.jfreechart.data;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 2, 2009 2:54:53 PM
 */
public class BaseChartPanel extends ChartPanel implements XmlConvertible<BaseChartPanel> {
    /**
     *
     */
    private static final long serialVersionUID = -7489463379706512333L;
    public BaseChartPanel (JFreeChart chart, int width, int height,
                           int minimumDrawWidth, int minimumDrawHeight,
                           int maximumDrawWidth, int maximumDrawHeight,
                           boolean useBuffer, boolean properties,
                           boolean save, boolean print, boolean zoom, boolean tooltips)
    {
        super(chart, width, height, minimumDrawWidth, minimumDrawHeight,
                maximumDrawWidth, maximumDrawHeight, useBuffer, properties, save,
                print, zoom, tooltips);
    }

    public BaseChartPanel (JFreeChart chart, boolean properties, boolean save,
                           boolean print, boolean zoom, boolean tooltips)
    {
        this(chart,
             DEFAULT_WIDTH,
             DEFAULT_HEIGHT,
             DEFAULT_MINIMUM_DRAW_WIDTH,
             DEFAULT_MINIMUM_DRAW_HEIGHT,
             DEFAULT_MAXIMUM_DRAW_WIDTH,
             DEFAULT_MAXIMUM_DRAW_HEIGHT,
             DEFAULT_BUFFER_USED,
             properties,
             save,
             print,
             zoom,
             tooltips
        );
    }

    public BaseChartPanel (JFreeChart chart, boolean useBuffer)
    {
        this(chart,
             DEFAULT_WIDTH,
             DEFAULT_HEIGHT,
             DEFAULT_MINIMUM_DRAW_WIDTH,
             DEFAULT_MINIMUM_DRAW_HEIGHT,
             DEFAULT_MAXIMUM_DRAW_WIDTH,
             DEFAULT_MAXIMUM_DRAW_HEIGHT,
             useBuffer,
             true,  // properties
             true,  // save
             true,  // print
             true,  // zoom
             true   // tooltips
        );
    }

    public BaseChartPanel (JFreeChart chart)
    {
        this(chart,
             DEFAULT_WIDTH,
             DEFAULT_HEIGHT,
             DEFAULT_MINIMUM_DRAW_WIDTH,
             DEFAULT_MINIMUM_DRAW_HEIGHT,
             DEFAULT_MAXIMUM_DRAW_WIDTH,
             DEFAULT_MAXIMUM_DRAW_HEIGHT,
             DEFAULT_BUFFER_USED,
             true,  // properties
             true,  // save
             true,  // print
             true,  // zoom
             true   // tooltips
            );
    }

    public XmlProxyConvertible<? extends ChartPanel> getPanelConverter (Element elem)
    {
        return (null == elem) ? null : ChartPanelReflectiveProxy.CHRTPNL;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseChartPanel fromXml (Element elem) throws Exception
    {
        final XmlProxyConvertible<? extends ChartPanel>    p=getPanelConverter(elem);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Object                                    o=
            (null == p) ? this : ((XmlProxyConvertible) p).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");

        return this;
    }

    public BaseChartPanel (Element elem) throws Exception
    {
        this((JFreeChart) null);

        final Object    o=fromXml(elem);    // apply rest of attributes
        if (o != this)
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        throw new UnsupportedOperationException("toXml() N/A");
    }
}
