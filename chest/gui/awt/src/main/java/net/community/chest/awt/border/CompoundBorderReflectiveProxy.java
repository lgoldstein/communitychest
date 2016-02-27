/*
 * 
 */
package net.community.chest.awt.border;

import java.lang.reflect.Method;
import java.util.Map;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import net.community.chest.CoVariantReturn;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;

import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @param <B> Type of {@link CompoundBorder} being reflected
 * @author Lyor G.
 * @since Jul 1, 2009 8:55:07 AM
 */
public class CompoundBorderReflectiveProxy<B extends CompoundBorder> extends AbstractBorderReflectiveProxy<B> {
	protected CompoundBorderReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public CompoundBorderReflectiveProxy (Class<B> objClass)
			throws IllegalArgumentException
	{
		this(objClass, false);
	}
	/*
	 * @see net.community.chest.awt.border.AbstractBorderReflectiveProxy#handleUnknownAttribute(javax.swing.border.AbstractBorder, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	protected B handleUnknownAttribute (B src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
	{
		Boolean	useInside=null;
		if (CompoundBorderFieldsAccessor.INSIDE_BORDER_FLDNAME.equalsIgnoreCase(name))
			useInside = Boolean.TRUE;
		else if (CompoundBorderFieldsAccessor.OUTSIDE_BORDER_FLDNAME.equalsIgnoreCase(name))
			useInside = Boolean.FALSE;
		if (useInside != null)
		{
			final ValueStringInstantiator<? extends Border>	vsi=
					resolveAttributeInstantiator(name, Border.class);
			final Border									b=vsi.newInstance(value);
			return ExtendedCompoundBorder.setBorder(src, b, useInside.booleanValue());
		}

		return super.handleUnknownAttribute(src, name, value, accsMap);
	}

	public Border setBorder (final B src, final Element elem) throws Exception
	{
		final String	borderType=elem.getAttribute(NAME_ATTR);
		final boolean	useInside;
		if ("inside".equalsIgnoreCase(borderType))
			useInside = true;
		else if ("outside".equalsIgnoreCase(borderType))
			useInside = false;
		else
			throw new IllegalArgumentException("setBorder(" + DOMUtils.toString(elem) + ") unknown compound border type: " + borderType);

		final XmlValueInstantiator<? extends Border>	proxy=getBorderProxy(elem);
		final Border									b=(null == proxy) ? null : proxy.fromXml(elem);
		if (b != null)
			ExtendedCompoundBorder.setBorder(src, b, useInside);

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

	public static final CompoundBorderReflectiveProxy<CompoundBorder>	COMPOUND=
		new CompoundBorderReflectiveProxy<CompoundBorder>(CompoundBorder.class, true) {

			/*
			 * @see net.community.chest.dom.proxy.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			@CoVariantReturn
			public ExtendedCompoundBorder createInstance (Element elem) throws Exception
			{
				return new ExtendedCompoundBorder();
			}
		};
}
