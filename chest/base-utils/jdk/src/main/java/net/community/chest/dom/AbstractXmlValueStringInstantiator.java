/*
 * 
 */
package net.community.chest.dom;

import org.w3c.dom.Element;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.transform.XmlValueInstantiator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Useful base class for instantiating objects from an XML {@link Element}
 * by simply using their {@link String} value
 * @param <V> Instantiated value type
 * @author Lyor G.
 * @since Aug 27, 2008 10:42:24 AM
 */
public abstract class AbstractXmlValueStringInstantiator<V>
			extends BaseTypedValuesContainer<V>
			implements ValueStringInstantiator<V>, XmlValueInstantiator<V> {
	protected AbstractXmlValueStringInstantiator (Class<V> objClass) throws IllegalArgumentException
	{
		super(objClass);
	}
	/**
	 * XML {@link Element} attribute used to extract the {@link String} 
	 */
	public static final String	VALUE_ATTR="value";
	/**
	 * Called by default implementation of {@link #fromXml(Element)}
	 * in order to extract the {@link String} value to be passed to the
	 * {@link #newInstance(String)} call
	 * @param elem The XML {@link Element} to be used
	 * @return <P>The resolved {@link String} - ignored if <code>null</code>.
	 * The default implementation looks for the value in the following
	 * order:</P></BR>
	 * <UL>
	 * 		<LI>
	 * 		Check if the XML {@link Element} has a text following it - e.g.,:</BR></BR>
	 * 		<P>
	 * 		&lt;item ...&gt;This is a text&lt;/item&gt;
	 * 		</P></BR>
	 * 		</LI>
	 * 
	 * 		<LI>
	 * 		Check if the XML {@link Element} has an attribute named
	 * 		{@link #VALUE_ATTR}
	 * 		</LI>
	 * </UL>
	 * @throws Exception if cannot resolve the value - the default
	 * implementation throws an {@link IllegalStateException} if none of the
	 * default options yielded any non-null/empty {@link String}
	 */
	public String resolveValueString (final Element elem) throws Exception
	{
		String	valText=null;
		if (((valText=DOMUtils.getElementStringValue(elem)) != null) && (valText.length() > 0))
			return valText;
		if (((valText=elem.getAttribute(VALUE_ATTR)) != null) && (valText.length() > 0))
			return valText;
		
		throw new IllegalStateException("resolveValueString(" + DOMUtils.toString(elem) + ") no text value available");
	}
	/*
	 * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public V fromXml (final Element elem) throws Exception
	{
		final String	s=resolveValueString(elem);
		if ((null == s) || (s.length() <= 0))
			return null;
		
		return newInstance(s);
	}
}
