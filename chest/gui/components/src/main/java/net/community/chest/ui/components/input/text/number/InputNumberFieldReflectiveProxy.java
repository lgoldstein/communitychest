/*
 * 
 */
package net.community.chest.ui.components.input.text.number;

import java.text.Format;
import java.text.NumberFormat;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.text.NumberFormatReflectiveProxy;
import net.community.chest.ui.helpers.text.InputTextFieldReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <N> Type of {@link Number} being reflected
 * @param <F> Type of {@link InputNumberField} being reflected
 * @author Lyor G.
 * @since Jan 12, 2009 2:37:36 PM
 */
public class InputNumberFieldReflectiveProxy<N extends Number & Comparable<N>, F extends InputNumberField<N>>
		extends InputTextFieldReflectiveProxy<F> {
	protected InputNumberFieldReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public InputNumberFieldReflectiveProxy (Class<F> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}
	// some attributes of interest
	public static final String	NUMBER_FORMAT_ATTR="NumberFormat",
								MIN_VALUE_ATTR="MinValue",
								MAX_VALUE_ATTR="MaxValue";
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#getAttributeInstantiator(java.lang.Object, java.lang.String, java.lang.Class)
	 */
	@Override
	protected ValueStringInstantiator<?> getAttributeInstantiator (F src, String name, Class<?> type) throws Exception
	{
		if (MIN_VALUE_ATTR.equalsIgnoreCase(name)
		 || MAX_VALUE_ATTR.equalsIgnoreCase(name))
		{
			final Class<N>	numClass=src.getValuesClass();
			return ClassUtil.getAtomicStringInstantiator(numClass);
		}

		return super.getAttributeInstantiator(src, name, type);
	}

	public static final String	NUMBER_FORMAT_ELEM_NAME=Format.class.getSimpleName().toLowerCase();
	public boolean isNumberFormatElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, NUMBER_FORMAT_ELEM_NAME);
	}

	public XmlValueInstantiator<? extends NumberFormat> getNumberFormatConverter (Element elem)
	{
		return (null == elem) ? null : NumberFormatReflectiveProxy.getNumberFormatConverter(elem);
	}

	public NumberFormat setNumberFormat (F src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends NumberFormat>	proxy=getNumberFormatConverter(elem);
		final NumberFormat									fmt=(null == proxy) ? null : proxy.fromXml(elem);
		if (fmt != null)
			src.setNumberFormat(fmt);
		return fmt;
	}
	/*
	 * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
	 */
	@Override
	public F fromXmlChild (F src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isNumberFormatElement(elem, tagName))
		{
			setNumberFormat(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final Class<?> getNumberFieldClass (final Element elem)
	{
		final String	numType=(null == elem) ? null : elem.getAttribute(CLASS_ATTR);
		final Class<?>	numClass=ClassUtil.getPrimitiveClass(numType);
		if (numClass != null)
		{
			if (Number.class.isAssignableFrom(numClass)
			 && Comparable.class.isAssignableFrom(numClass))
				return numClass;
		}

		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final InputNumberFieldReflectiveProxy	INPNUMFLD=
		new InputNumberFieldReflectiveProxy(InputNumberField.class) {
			/* Uses the "class" attribute to find out which field type is required
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXml(org.w3c.dom.Element)
			 */
			@Override
			public InputNumberField fromXml (Element elem) throws Exception
			{
				final Class<?>	numClass=getNumberFieldClass(elem);
				if (null == numClass)
					throw new ClassNotFoundException("fromXml(" + DOMUtils.toString(elem) + ") unknown class");

				return new InputNumberField(numClass, elem, true /* auto-layout since have the element */);
			}
		};
}
