/*
 * 
 */
package net.community.chest.jfree.jfreechart.chart.renderer;

import java.lang.reflect.Method;

import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.jfree.jfreechart.plot.category.CategoryItemLabelGeneratorConverter;
import net.community.chest.jfree.jfreechart.plot.category.CategoryToolTipGeneratorConverter;
import net.community.chest.jfree.jfreechart.plot.category.StandardCategoryURLGeneratorConverter;

import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <R> Type of {@link AbstractCategoryItemRenderer} being reflected
 * @author Lyor G.
 * @since Jun 8, 2009 12:04:10 PM
 */
public class AbstractCategoryItemRendererReflectiveProxy<R extends AbstractCategoryItemRenderer> extends AbstractRendererReflectiveProxy<R> {
	protected AbstractCategoryItemRendererReflectiveProxy (Class<R> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	BASE_TTPGEN_ATTR="ToolTipGenerator",
								BASE_ITEMURLGEN_ATTR="URLGenerator",
								BASE_ITEMLBLGEN_ATTR="LabelGenerator";
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected R updateObjectAttribute (R src, String name, String value, Method setter) throws Exception
	{
		if (BASE_ITEMURLGEN_ATTR.equalsIgnoreCase(name))
		{
			final CategoryURLGenerator	g=StandardCategoryURLGeneratorConverter.fromString(value);
			if (g != null)
				setter.invoke(src, g);
			return src;
		}

		return super.updateObjectAttribute(src, name, value, setter);
	}

	public static final boolean isBaseCategoryToolTipGeneratorElement (Element elem, String tagName)
	{
		return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, BASE_TTPGEN_ATTR);
	}

	public XmlValueInstantiator<? extends CategoryToolTipGenerator> getBaseCategoryToolTipGeneratorConverter (Element elem)
	{
		return (null == elem) ? null : CategoryToolTipGeneratorConverter.DEFAULT;
	}
	// for now only StandardPieSectionLabelGenerator is supported
	public CategoryToolTipGenerator setBaseCategoryToolTipGenerator (R src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends CategoryToolTipGenerator>	p=getBaseCategoryToolTipGeneratorConverter(elem);
		final CategoryToolTipGenerator										g=(null == p) ? null : p.fromXml(elem);
		if (g != null)
			src.setBaseToolTipGenerator(g);

		return g;
	}

	public static final boolean isBaseItemURLGeneratorElement (Element elem, String tagName)
	{
		return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, BASE_ITEMURLGEN_ATTR);
	}

	public XmlValueInstantiator<? extends CategoryURLGenerator> getBaseItemURLGeneratorConverter (Element elem)
	{
		return (null == elem) ? null : StandardCategoryURLGeneratorConverter.DEFAULT;
	}
	// for now only StandardPieSectionLabelGenerator is supported
	public CategoryURLGenerator setBaseItemURLGenerator (R src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends CategoryURLGenerator>	p=getBaseItemURLGeneratorConverter(elem);
		final CategoryURLGenerator										g=(null == p) ? null : p.fromXml(elem);
		if (g != null)
			src.setBaseItemURLGenerator(g);

		return g;
	}

	public static final boolean isBaseItemLabelGeneratorElement (Element elem, String tagName)
	{
		return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, BASE_ITEMLBLGEN_ATTR);
	}

	public XmlValueInstantiator<? extends CategoryItemLabelGenerator> getBaseItemLabelGenerator (Element elem)
	{
		return (null == elem) ? null : CategoryItemLabelGeneratorConverter.DEAULT;
	}

	public CategoryItemLabelGenerator setBaseItemLabelGenerator (R src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends CategoryItemLabelGenerator>	p=getBaseItemLabelGenerator(elem);
		final CategoryItemLabelGenerator									g=(null == p) ? null : p.fromXml(elem);
		if (g != null)
			src.setBaseItemLabelGenerator(g);

		return g;
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public R fromXmlChild (R src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isStrokeElement(elem, tagName))
		{
			setStrokeValue(src, elem);
			return src;
		}
		else if (isBaseItemLabelGeneratorElement(elem, tagName))
		{
			setBaseItemLabelGenerator(src, elem);
			return src;
		}
		else if (isBaseCategoryToolTipGeneratorElement(elem, tagName))
		{
			setBaseCategoryToolTipGenerator(src, elem);
			return src;
		}
		else if (isBaseItemURLGeneratorElement(elem, tagName))
		{
			setBaseItemURLGenerator(src, elem);
			return src;
		}
		
		return super.fromXmlChild(src, elem);
	}
}
