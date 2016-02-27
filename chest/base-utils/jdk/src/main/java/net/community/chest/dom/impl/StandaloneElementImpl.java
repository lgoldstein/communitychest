package net.community.chest.dom.impl;

import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful implementation for {@link Element}-s that is "standalone" - i.e.,
 * not really part of any real XML {@link Document} but provides a lot of
 * useful {@link Element} implementation that can be used to "mask" some
 * non-XML data into such.</P>
 * 
 * @author Lyor G.
 * @since Aug 7, 2007 8:58:28 AM
 */
public class StandaloneElementImpl extends BaseNodeImpl<StandaloneElementImpl> implements Element {
	/*
	 * @see org.w3c.dom.Element#getTagName()
	 */
	@Override
	public String getTagName ()
	{
		return getNodeName();
	}

	public void setTagName (String tagName)
	{
		setNodeName(tagName);
	}
	/*
	 * @see org.w3c.dom.Node#setNodeValue(java.lang.String)
	 */
	@Override
	public void setNodeValue (String nodeValue) throws DOMException
	{
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getArgumentsExceptionLocation("setNodeValue", nodeValue) + " N/A");
	}
	/*
	 * @see net.community.chest.dom.BaseNodeImpl#setOwnerDocument(org.w3c.dom.Document)
	 */
	@Override
	public void setOwnerDocument (Document owner)
	{
		super.setOwnerDocument(owner);
		setParentNode(owner);
	}

	public StandaloneElementImpl (final Document owner, final String tagName)
	{
		super(StandaloneElementImpl.class, tagName);

		setOwnerDocument(owner);
	}

	public StandaloneElementImpl (final String tagName)
	{
		this(null, tagName);
	}

	public StandaloneElementImpl (final Document owner)
	{
		this(owner, null);
	}

	public StandaloneElementImpl ()
	{
		this((String) null);
	}
	/*
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	@Override
	public final /* no cheating */ short getNodeType ()
	{
		return Node.ELEMENT_NODE;
	}
	/*
	 * @see org.w3c.dom.Element#getAttributeNode(java.lang.String)
	 */
	@Override
	public Attr getAttributeNode (final String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;

		final NamedNodeMap	m=getAttributes();
		if ((null == m) || (m.getLength() <= 0))
			return null;

		final Node	n=m.getNamedItem(name);
		if ((n != null) && (n.getNodeType() != Node.ATTRIBUTE_NODE))
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "getAttributeNode(" + name + ") node not an attribute but rather a " + n.getClass().getName());

		return (Attr) n;
	}
	/*
	 * @see org.w3c.dom.Element#setAttributeNode(org.w3c.dom.Attr)
	 */
	@Override
	public Attr setAttributeNode (final Attr newAttr) throws DOMException
	{
		final NamedNodeMap	m=getAttributes(true);
		if (null == m)
			throw new DOMException(DOMException.INVALID_STATE_ERR, "setAttributeNode(" + newAttr + ") no map created");

		final Node	n=m.setNamedItem(newAttr);
		if ((n != null) && (n.getNodeType() != Node.ATTRIBUTE_NODE))
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "setAttributeNode(" + newAttr + ") previous node not an attribute but rather a " + n.getClass().getName());

		return (Attr) n;
	}
	/*
	 * @see org.w3c.dom.Element#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute (final String name)
	{
		final Attr	a=getAttributeNode(name);
		return (null == a) ? null : a.getValue();
	}
	/*
	 * @see org.w3c.dom.Element#setAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void setAttribute (final String name, final String value) throws DOMException
	{
		if ((null == name) || (name.length() <= 0)
		 || (null == value) || (value.length() <= 0))
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, getArgumentsExceptionLocation("setAttribute", name, value) + " null/empty attribute name/value");

		setAttributeNode(new StandaloneAttrImpl(this, name, value));
	}
	/*
	 * @see org.w3c.dom.Element#hasAttribute(java.lang.String)
	 */
	@Override
	public boolean hasAttribute (String name)
	{
		return (getAttribute(name) != null);
	}
	/*
	 * @see org.w3c.dom.Element#setAttributeNodeNS(org.w3c.dom.Attr)
	 */
	@Override
	public Attr setAttributeNodeNS (Attr newAttr) throws DOMException
	{
		final NamedNodeMap	m=getAttributes(true);
		if (null == m)
			throw new DOMException(DOMException.INVALID_STATE_ERR, "setAttributeNodeNS(" + newAttr + ") no map created");

		final Node	n=m.setNamedItemNS(newAttr);
		if ((n != null) && (n.getNodeType() != Node.ATTRIBUTE_NODE))
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "setAttributeNodeNS(" + newAttr + ") previous node not an attribute but rather a " + n.getClass().getName());

		return (Attr) n;
	}
	/*
	 * @see org.w3c.dom.Element#setAttributeNS(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setAttributeNS (final String namespaceURI, final String qualifiedName, String value) throws DOMException
	{
		if ((null == namespaceURI) || (namespaceURI.length() <= 0)
		 || (null == qualifiedName) || (qualifiedName.length() <= 0)
		 || (null == value) || (value.length() <= 0))
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, getArgumentsExceptionLocation("setAttributeNS", namespaceURI, qualifiedName, value) + " null/empty attribute NS/name/value");
		
		setAttributeNodeNS(new StandaloneAttrImpl(this, namespaceURI, qualifiedName, value));
	}
	/*
	 * @see org.w3c.dom.Element#getAttributeNodeNS(java.lang.String, java.lang.String)
	 */
	@Override
	public Attr getAttributeNodeNS (final String namespaceURI, final String localName) throws DOMException
	{
		final NamedNodeMap	m=getAttributes();
		if (null == m)
			return null;

		final Node	n=m.getNamedItemNS(namespaceURI, localName);
		if ((n != null) && (n.getNodeType() != Node.ATTRIBUTE_NODE))
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "getAttributeNodeNS(" + namespaceURI + ":" + localName + ") not an attribute: " + n.getClass().getName());

		return (Attr) n;
	}
	/*
	 * @see org.w3c.dom.Element#hasAttributeNS(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean hasAttributeNS (String namespaceURI, String localName) throws DOMException
	{
		return (getAttributeNodeNS(namespaceURI, localName) != null);
	}
	/*
	 * @see org.w3c.dom.Element#getAttributeNS(java.lang.String, java.lang.String)
	 */
	@Override
	public String getAttributeNS (String namespaceURI, String localName) throws DOMException
	{
		final Attr	a=getAttributeNodeNS(namespaceURI, localName);
		return (null == a) ? null : a.getValue();
	}
	/*
	 * @see org.w3c.dom.Element#getElementsByTagNameNS(java.lang.String, java.lang.String)
	 */
	@Override
	public NodeList getElementsByTagNameNS (String namespaceURI, String localName) throws DOMException
	{
		return DOMUtils.getElementsByTagNameNS(this, namespaceURI, localName);
	}
	/*
	 * @see org.w3c.dom.Element#getElementsByTagName(java.lang.String)
	 */
	@Override
	public NodeList getElementsByTagName (String name)
	{
		return DOMUtils.getElementsByTagName(this, name);
	}

	private TypeInfo	_typeInfo	/* =null */;
	/*
	 * @see org.w3c.dom.Attr#getSchemaTypeInfo()
	 */
	@Override
	public TypeInfo getSchemaTypeInfo ()
	{
		return _typeInfo;
	}

	public void setSchemaTypeInfo (TypeInfo typeInfo)
	{
		_typeInfo = typeInfo;
	}
	/*
	 * @see org.w3c.dom.Element#removeAttributeNode(org.w3c.dom.Attr)
	 */
	@Override
	public Attr removeAttributeNode (Attr oldAttr) throws DOMException
	{
		if (null == oldAttr)
			return null;

		final String		name=oldAttr.getName();
		final NamedNodeMap	m=getAttributes();
		final Node			n=
			((m != null) && (m.getLength() > 0)) ? m.removeNamedItem(name) : null;
		if ((n != null) && (n.getNodeType() != Node.ATTRIBUTE_NODE))
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "removeAttributeNode(" + oldAttr + ") removed node is a " + n.getClass().getName());

		return (Attr) n;
	}
	/*
	 * @see org.w3c.dom.Element#removeAttribute(java.lang.String)
	 */
	@Override
	public void removeAttribute (final String name) throws DOMException
	{
		final NamedNodeMap	m=getAttributes();
		final Node			n=
			((m != null) && (m.getLength() > 0)) ? m.removeNamedItem(name) : null;
		if (null == n)
			return;

		if (n.getNodeType() != Node.ATTRIBUTE_NODE)	// debug breakpoint
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "removeAttribute(" + name + ") removed node is a " + n.getClass().getName());
	}
	/*
	 * @see org.w3c.dom.Element#removeAttributeNS(java.lang.String, java.lang.String)
	 */
	@Override
	public void removeAttributeNS (String namespaceURI, String localName) throws DOMException
	{
		final NamedNodeMap	m=getAttributes();
		final Node			n=
			((m != null) && (m.getLength() > 0)) ? m.removeNamedItemNS(namespaceURI, localName) : null;
		if (null == n)
			return;

		if (n.getNodeType() != Node.ATTRIBUTE_NODE)	// debug breakpoint
			throw new DOMException(DOMException.TYPE_MISMATCH_ERR, "removeAttributeNS(" + namespaceURI + ":" + localName + ") removed node is a " + n.getClass().getName());
	}
	/*
	 * @see org.w3c.dom.Element#setIdAttribute(java.lang.String, boolean)
	 */
	@Override
	public void setIdAttribute (String name, boolean isId) throws DOMException
	{
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getArgumentsExceptionLocation("setIdAttribute", name, Boolean.valueOf(isId)) + " N/A");
	}
	/*
	 * @see org.w3c.dom.Element#setIdAttributeNode(org.w3c.dom.Attr, boolean)
	 */
	@Override
	public void setIdAttributeNode (Attr idAttr, boolean isId) throws DOMException
	{
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getArgumentsExceptionLocation("setIdAttributeNode", idAttr, Boolean.valueOf(isId)) + " N/A");
	}
	/*
	 * @see org.w3c.dom.Element#setIdAttributeNS(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void setIdAttributeNS (String namespaceURI, String localName, boolean isId) throws DOMException
	{
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getArgumentsExceptionLocation("setIdAttributeNS", namespaceURI, localName, Boolean.valueOf(isId)) + " N/A");
	}
	/*
	 * @see net.community.chest.dom.BaseNodeImpl#toString()
	 */
	@Override
	public String toString ()
	{
		final String		tagName=getTagName();
		final NamedNodeMap	attrs=getAttributes();
		final int			numAttrs=(null == attrs) ? 0 : attrs.getLength(),
							tagLen=(null == tagName) ? 0 : tagName.length();
		final StringBuilder	sb=new StringBuilder(Math.max(16, tagLen) + Math.max(numAttrs, 1) * 32 + 16);
		sb.append('<').append(tagName);

		for (int	aIndex=0; aIndex < numAttrs; aIndex++)
		{
			final Node	n=attrs.item(aIndex);
			if ((null == n) || (n.getNodeType() != Node.ATTRIBUTE_NODE))
				continue;

			final String	aName=n.getNodeName(), aValue=n.getNodeValue();
			sb.append(' ')
			  .append(aName)
			  .append("=\"")
			  .append(aValue)
			  .append('"')
			  ;
		}

		sb.append("/>");
		return sb.toString();
	}
}
