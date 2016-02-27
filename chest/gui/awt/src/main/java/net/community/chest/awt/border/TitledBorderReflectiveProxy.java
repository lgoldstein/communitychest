/*
 * 
 */
package net.community.chest.awt.border;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.community.chest.CoVariantReturn;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.AttributeMethodType;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link TitledBorder} type
 * @author Lyor G.
 * @since Dec 10, 2008 9:06:03 AM
 */
public class TitledBorderReflectiveProxy<B extends TitledBorder> extends AbstractBorderReflectiveProxy<B> {
	public TitledBorderReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected TitledBorderReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	// special handling for attributes
	public static final String	POSITION_ATTR="TitlePosition",
								JUSTIFICATION_ATTR="TitleJustification",
								TITLE_ATTR="Title",
								BORDER_ATTR=Border.class.getSimpleName();
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if (POSITION_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) TitledBorderPositionValueStringInstantiator.DEFAULT;
		else if (JUSTIFICATION_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) TitledBorderJustificationValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}

	public static final String	TITLE_ALIAS_PREFIX=TITLE_ATTR;
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#getEffectiveAttributeName(java.lang.String)
	 */
	@Override
	public String getEffectiveAttributeName (final String name)
	{
		if (BORDER_ATTR.equalsIgnoreCase(name)
		 || TITLE_ATTR.equalsIgnoreCase(name)
		 || CLASS_ATTR.equalsIgnoreCase(name)
		 || NAME_ATTR.equalsIgnoreCase(name))
			return name;	// these attributes have no prefix

		// check if attribute is prefixed correctly
		if (StringUtil.startsWith(name, TITLE_ALIAS_PREFIX, true, false))
			return name;

		return TITLE_ALIAS_PREFIX + AttributeMethodType.getAdjustedAttributeName(name);
	}

	public Border setBorder (final B src, final Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends Border>	proxy=getBorderProxy(elem);
		final Border									b=(null == proxy) ? null : proxy.fromXml(elem);
		if (b != null)
			src.setBorder(b);

		return b;
	}
	/*
	 * @see net.community.chest.dom.proxy.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public B fromXmlChild (B src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isBorderElement(elem, tagName))
		{
			setBorder(src, elem);
			return src;
		}
		
		return super.fromXmlChild(src, elem);
	}

	public static final TitledBorderReflectiveProxy<TitledBorder>	TITLED=
			new TitledBorderReflectiveProxy<TitledBorder>(TitledBorder.class, true) {
				/*
				 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
				 */
				@Override
				@CoVariantReturn
				public ExtendedTitledBorder createInstance (Element elem) throws Exception
				{
					return new ExtendedTitledBorder(TITLE_ATTR);
				}
		};
}
