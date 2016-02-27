/*
 * 
 */
package net.community.chest.jfree.jfreechart.chart;

import java.awt.Font;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.awt.font.FontValueInstantiator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.jfree.jcommon.ui.AlignTypeValueStringInstantiator;
import net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy;
import net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy;
import net.community.chest.jfree.jfreechart.title.TextTitleReflectiveProxy;
import net.community.chest.util.map.MapEntryImpl;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.title.TextTitle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <C> The reflected {@link JFreeChart} type
 * @author Lyor G.
 * @since Jan 27, 2009 3:18:18 PM
 */
public class JFreeChartReflectiveProxy<C extends JFreeChart> extends ChartReflectiveAttributesProxy<C> {
	protected JFreeChartReflectiveProxy (Class<C> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public JFreeChartReflectiveProxy (Class<C> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final String	BG_IMG_ATTR="BackgroundImage",
									BG_IMG_ALIAS="bgImage",
								BG_IMG_ALIGN_ATTR="BackgroundImageAlignment",
									BG_IMG_ALIGN_ALIAS="bgImageAlign";
	/*
	 * @see net.community.chest.dom.proxy.AbstractXmlProxyConverter#initializeAliasesMap(java.util.Map)
	 */
	@Override
	protected Map<String,String> initializeAliasesMap (Map<String,String> org)
	{
		return addAttributeAliases(super.initializeAliasesMap(org),
				BG_IMG_ALIAS, BG_IMG_ATTR,
				BG_IMG_ALIGN_ALIAS, BG_IMG_ALIGN_ATTR
			);
	}
	/*
	 * @see net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <Z> ValueStringInstantiator<Z> resolveAttributeInstantiator (String name, Class<Z> type) throws Exception
	{
		if (BG_IMG_ALIGN_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<Z>) AlignTypeValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected C updateObjectAttribute (C src, String name, String value, Method setter) throws Exception
	{
		if (BG_IMG_ATTR.equalsIgnoreCase(name))
			return updateObjectResourceAttribute(src, name, value, setter);

		return super.updateObjectAttribute(src, name, value, setter);
	}
	// virtual attributes to be ignored
	public static final String	TITLE_FONT_VIRTATTR="titleFont",
								LEGEND_VIRTATTR="legend";

	public static final Map.Entry<String,Font> getTitleFontValue (final Element elem, final String aName) throws Exception
	{
		if (null == elem)
			return null;
		if ((null == aName) || (aName.length() <= 0))
			throw new IllegalArgumentException("getTitleFontValue(" + DOMUtils.toString(elem) + ") no attribute name specified");

		final String	v=elem.getAttribute(aName);
		if ((null == v) || (v.length() <= 0))
			return null;	// OK if no value

		final Font	f=FontValueInstantiator.DEFAULT.newInstance(v);
		if (null == f)
			throw new NoSuchElementException("getTitleFontValue(" + DOMUtils.toString(elem) + ")[" + aName + "] unknown value: " + v);

		return new MapEntryImpl<String,Font>(v, f);
	}

	public static final Map.Entry<String,Font> getTitleFontValue (final Element elem) throws Exception
	{
		return getTitleFontValue(elem, TITLE_FONT_VIRTATTR);
	}

	public static final Font resolveTitleFontValue (final Element elem, final String aName) throws Exception
	{
		final Map.Entry<String,Font>	fp=getTitleFontValue(elem, aName);
		final Font						f=(null == fp) ? null : fp.getValue();
		if (null == f)
			return JFreeChart.DEFAULT_TITLE_FONT;

		return f;
	}

	public static final Font resolveTitleFontValue (final Element elem) throws Exception
	{
		return resolveTitleFontValue(elem, TITLE_FONT_VIRTATTR);
	}

	public static final Map.Entry<String,Boolean> getLegendValue (final Element elem, final String aName)
	{
		if (null == elem)
			return null;
		if ((null == aName) || (aName.length() <= 0))
			throw new IllegalArgumentException("getLegendValue(" + DOMUtils.toString(elem) + ") no attribute name specified");

		final String	v=elem.getAttribute(aName);
		if ((null == v) || (v.length() <= 0))
			return null;	// OK if no value

		final Boolean	b=Boolean.valueOf(v);
		return new MapEntryImpl<String,Boolean>(v, b);
	}

	public static final Map.Entry<String,Boolean> getLegendValue (final Element elem)
	{
		return getLegendValue(elem, LEGEND_VIRTATTR);
	}

	public static final boolean resolveLegendValue (Element elem, String aName)
	{
		final Map.Entry<String,Boolean>	vp=getLegendValue(elem, aName);
		final Boolean					bv=(null == vp) ? null : vp.getValue();
		if (null == bv)
			return true;

		return bv.booleanValue();
	}

	public static final boolean resolveLegendValue (Element elem)
	{
		return resolveLegendValue(elem, LEGEND_VIRTATTR);
	}
	/*
 	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#handleUnknownAttribute(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	protected C handleUnknownAttribute (C src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
	{
		if (TITLE_FONT_VIRTATTR.equalsIgnoreCase(name)
		 || LEGEND_VIRTATTR.equalsIgnoreCase(name))
			return src;

		return super.handleUnknownAttribute(src, name, value, accsMap);
	}

	public static final String	TITLE_ATTR="Title";
	public boolean isTitleElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, TITLE_ATTR);
	}

	public XmlValueInstantiator<? extends TextTitle> getTextTitleConverter (Element elem)
	{
		return (null == elem) ? null : TextTitleReflectiveProxy.getTextTitleConverter(elem);
	}

	public TextTitle setTitle (C src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends TextTitle>	proxy=getTextTitleConverter(elem);
		final TextTitle									t=(null == proxy) ? null : proxy.fromXml(elem);
		if (t != null)
			src.setTitle(t);
		return t;
	}

	public boolean isPlotElement (Element elem, String tagName)
	{
		return PlotReflectiveProxy.isPlotElement(elem, tagName);
	}
	/*
	 * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
	 */
	@Override
	public C fromXmlChild (C src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isTitleElement(elem, tagName))
		{
			setTitle(src, elem);
			return src;
		}
		else if (isStrokeElement(elem, tagName))
		{
			setStrokeValue(src, elem);
			return src;
		}
		else if (isPlotElement(elem, tagName))
			return src;	// ignored since handled in the construction

		return super.fromXmlChild(src, elem);
	}
	/**
	 * @param elem The XMl {@link Element} for the <U><B>chart</B></U>
	 * @return The XML <U>child</U> element to be used for the {@link Plot}
	 * value of the chart - <code>null</code> if not found
	 * @see PlotReflectiveProxy#isPlotElement(Element)
	 */
	public static final Element getChartPlotElement (Element elem)
	{
		final Collection<? extends Element>	el=DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE);
		if ((null == el) || (el.size() <= 0))
			return null;

		for (final Element ce : el)
		{
			if (PlotReflectiveProxy.isPlotElement(ce))
				return ce;
		}

		return null;
	}

	public static final Plot getChartPlotInstance (Element elem) throws Exception
	{
		final Element	pe=getChartPlotElement(elem);
		if (null == pe)
			return null;

		final XmlValueInstantiator<?>	p=PlotReflectiveProxy.getPlotConverter(pe);
		if (null == p)
			return null;

		return (Plot) p.fromXml(pe);
	}
	// default name of chart XML element(s)
	public static final String	CHART_ELEM_NAME="chart";

	public static final JFreeChartReflectiveProxy<JFreeChart>	CHART=
		new JFreeChartReflectiveProxy<JFreeChart>(JFreeChart.class, true) {
			/*
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			public JFreeChart createInstance (Element elem) throws Exception
			{
				final Plot		p=getChartPlotInstance(elem);
				final Font		f=resolveTitleFontValue(elem);
				final boolean	l=resolveLegendValue(elem);
				return new JFreeChart(null, f, p, l);
			}
		};
}
