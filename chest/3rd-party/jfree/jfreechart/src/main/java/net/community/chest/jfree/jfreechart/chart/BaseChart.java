/*
 * 
 */
package net.community.chest.jfree.jfreechart.chart;

import java.awt.Font;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 2, 2009 2:15:04 PM
 */
public class BaseChart extends JFreeChart implements XmlConvertible<BaseChart> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4127200327815366814L;
	public BaseChart (String title, Font titleFont, Plot plot, boolean createLegend)
	{
		super(title, titleFont, plot, createLegend);
	}

	public BaseChart (Plot plot)
	{
        this(null, null, plot, true);
	}

	public BaseChart (String title, Plot plot)
	{
        this(title, DEFAULT_TITLE_FONT, plot, true);
	}

	public XmlProxyConvertible<? extends JFreeChart> getChartConverter (Element elem)
	{
		return (null == elem) ? null : JFreeChartReflectiveProxy.CHART;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseChart fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<? extends JFreeChart>	p=getChartConverter(elem);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Object									o=
			(null == p) ? this : ((XmlProxyConvertible) p).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");

		return this;
	}

	public BaseChart (Element elem) throws Exception
	{
		this(null, // title (if any) will be taken care of by the "fromXml" call
			JFreeChartReflectiveProxy.resolveTitleFontValue(elem),
			JFreeChartReflectiveProxy.getChartPlotInstance(elem),
			JFreeChartReflectiveProxy.resolveLegendValue(elem));

		final Object	o=fromXml(elem);	// apply rest of attributes
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
