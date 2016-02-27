/*
 * 
 */
package net.community.chest.text;

import java.text.DateFormat;
import java.text.NumberFormat;
import net.community.chest.dom.transform.XmlValueInstantiator;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link DateFormat}
 * @author Lyor G.
 * @since Jan 12, 2009 3:33:42 PM
 */
public class DateFormatReflectiveProxy<F extends DateFormat> extends FormatReflectiveProxy<F> {
	protected DateFormatReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	NUMBER_FORMAT_ATTR=NumberFormat.class.getSimpleName();
	public boolean isNumberFormatElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, NUMBER_FORMAT_ATTR);
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
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
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
}
