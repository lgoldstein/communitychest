/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.pie;

import java.lang.reflect.Method;
import java.util.Collection;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.urls.PieURLGenerator;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> Reflected {@link PiePlot} type
 * @author Lyor G.
 * @since Feb 1, 2009 2:45:47 PM
 */
public class PiePlotReflectiveProxy<P extends PiePlot> extends PlotReflectiveProxy<P> {
	protected PiePlotReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
			throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public PiePlotReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}
	/*
	 * @see net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if ((type != null) && PieLabelLinkStyle.class.isAssignableFrom(type))
			return (ValueStringInstantiator<C>) PieLabelLinkStyleValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}

	protected PieSectionLabelGenerator createPieSectionLabelGenerator (String name, String value, Method setter) throws Exception
	{
		if ((null == name) || (name.length() <= 0)
		 || (null == value) || (value.length() <= 0)
		 || (null == setter))
			return null;

		return new StandardPieSectionLabelGenerator(value);
	}

	protected PieToolTipGenerator createPieToolTipGenerator (String name, String value, Method setter) throws Exception
	{
		if ((null == name) || (name.length() <= 0)
		 || (null == value) || (value.length() <= 0)
		 || (null == setter))
			return null;

		return new StandardPieToolTipGenerator(value);
	}

	protected PieURLGenerator createPieURLGenerator (String name, String value, Method setter) throws Exception
	{
		if ((null == name) || (name.length() <= 0)
		 || (null == value) || (value.length() <= 0)
		 || (null == setter))
			return null;

		final Collection<String>	vl=StringUtil.splitString(value, ',');
		final int					numArgs=(null == vl) ? 0 : vl.size();
		return StandardPieURLGeneratorConverter.DEFAULT.fromValues((numArgs <= 0) ? null : vl.toArray(new String[numArgs]));
	}

	public static final String	LBLGEN_ATTR="LabelGenerator",
								TTPGEN_ATTR="ToolTipGenerator",
								URLGEN_ATTR="URLGenerator";
	/*
	 * @see net.community.chest.jfree.jfreechart.plot.PlotReflectiveProxy#updateObjectAttribute(org.jfree.chart.plot.Plot, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected P updateObjectAttribute (P src, String name, String value, Method setter) throws Exception
	{
		if (LBLGEN_ATTR.equalsIgnoreCase(name))
		{
			final PieSectionLabelGenerator	g=createPieSectionLabelGenerator(name, value, setter);
			if (g != null)
				setter.invoke(src, g);
			return src;
		}
		else if (TTPGEN_ATTR.equalsIgnoreCase(name))
		{
			final PieToolTipGenerator	g=createPieToolTipGenerator(name, value, setter);
			if (g != null)
				setter.invoke(src, g);
			return src;
		}
		else if (URLGEN_ATTR.equalsIgnoreCase(name))
		{
			final PieURLGenerator	g=createPieURLGenerator(name, value, setter);
			if (g != null)
				setter.invoke(src, g);
			return src;
		}

		return super.updateObjectAttribute(src, name, value, setter);
	}

	public static final boolean isLabelGeneratorElement (Element elem, String tagName)
	{
		return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, LBLGEN_ATTR);
	}

	public XmlValueInstantiator<? extends PieSectionLabelGenerator> getPieSectionLabelGeneratorConverter (Element elem)
	{
		return (null == elem) ? null : StandardPieSectionLabelGeneratorConverter.DEFAULT;
	}
	// for now only StandardPieSectionLabelGenerator is supported
	public PieSectionLabelGenerator setLabelGenerator (P src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends PieSectionLabelGenerator>	p=getPieSectionLabelGeneratorConverter(elem);
		final PieSectionLabelGenerator									g=(null == p) ? null : p.fromXml(elem);
		if (g != null)
			src.setLabelGenerator(g);

		return g;
	}

	public static final boolean isTooltipGeneratorElement (Element elem, String tagName)
	{
		return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, TTPGEN_ATTR);
	}

	public XmlValueInstantiator<? extends PieToolTipGenerator> getTooltipGeneratorConverter (Element elem)
	{
		return (null == elem) ? null : StandardPieToolTipGeneratorConverter.DEFAULT;
	}
	// for now only StandardPieSectionLabelGenerator is supported
	public PieToolTipGenerator setTooltipGenerator (P src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends PieToolTipGenerator>	p=getTooltipGeneratorConverter(elem);
		final PieToolTipGenerator									g=(null == p) ? null : p.fromXml(elem);
		if (g != null)
			src.setToolTipGenerator(g);

		return g;
	}

	public static final boolean isURLGeneratorElement (Element elem, String tagName)
	{
		return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, URLGEN_ATTR);
	}

	public XmlValueInstantiator<? extends PieURLGenerator> getURLGeneratorConverter (Element elem)
	{
		return (null == elem) ? null : StandardPieURLGeneratorConverter.DEFAULT;
	}
	// for now only StandardPieSectionLabelGenerator is supported
	public PieURLGenerator setURLGenerator (P src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends PieURLGenerator>	p=getURLGeneratorConverter(elem);
		final PieURLGenerator								g=(null == p) ? null : p.fromXml(elem);
		if (g != null)
			src.setURLGenerator(g);

		return g;
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public P fromXmlChild (P src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isStrokeElement(elem, tagName))
		{
			setStrokeValue(src, elem);
			return src;
		}
		else if (isLabelGeneratorElement(elem, tagName))
		{
			setLabelGenerator(src, elem);
			return src;
		}
		else if (isTooltipGeneratorElement(elem, tagName))
		{
			setTooltipGenerator(src, elem);
			return src;
		}
		else if (isURLGeneratorElement(elem, tagName))
		{
			setURLGenerator(src, elem);
			return src;
		}
		
		return super.fromXmlChild(src, elem);
	}

	public static final PiePlotReflectiveProxy<PiePlot>	PIEPLOT=
			new PiePlotReflectiveProxy<PiePlot>(PiePlot.class, true);
}
