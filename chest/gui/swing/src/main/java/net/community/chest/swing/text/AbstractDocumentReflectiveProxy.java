/*
 * 
 */
package net.community.chest.swing.text;

import java.util.Dictionary;
import java.util.Properties;

import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;

import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <D> Type of {@link AbstractDocument} being reflected
 * @author Lyor G.
 * @since Nov 11, 2010 2:25:59 PM
 */
public abstract class AbstractDocumentReflectiveProxy<D extends AbstractDocument> extends DocumentReflectiveProxy<D> {
	protected AbstractDocumentReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	FILTER_ELEM_NAME="filter";
	public boolean isFilterElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, FILTER_ELEM_NAME);
	}
	
	protected DocumentFilter createDocumentFilter (Element elem) throws Exception
	{
		throw new UnsupportedOperationException("createDocumentFilter(" + DOMUtils.toString(elem) + ") N/A");	// TODO implement it
	}

	protected DocumentFilter setDocumentFilter (D src, Element elem) throws Exception
	{
		final DocumentFilter	f=createDocumentFilter(elem);
		if (f != null)
			src.setDocumentFilter(f);
		return f;
	}

	public static final String	PROPERTIES_ELEM_NAME=Properties.class.getSimpleName().toLowerCase();
	public boolean isPropertiesElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, PROPERTIES_ELEM_NAME);
	}

	protected Dictionary<Object,Object> createDocumentProperties (Element elem) throws Exception
	{
		throw new UnsupportedOperationException("createDocumentProperties(" + DOMUtils.toString(elem) + ") N/A");	// TODO implement it
	}
	
	protected Dictionary<Object,Object> setDocumentProperties (D src, Element elem) throws Exception
	{
		final Dictionary<Object,Object>	props=createDocumentProperties(elem);
		if (props != null)
			src.setDocumentProperties(props);
		return props;
	}
	/*
	 * @see net.community.chest.dom.proxy.AbstractXmlProxyConverter#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public D fromXmlChild (D src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isFilterElement(elem, tagName))
		{
			setDocumentFilter(src, elem);
			return src;
		}
		else if (isPropertiesElement(elem, tagName))
		{
			setDocumentProperties(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

}
