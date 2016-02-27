package net.community.chest.dom.impl;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful "standalone" implementation of the W3C {@link Attr} interface as
 * a Java bean</P>
 * 
 * @author Lyor G.
 * @since Aug 7, 2007 9:07:49 AM
 */
public class StandaloneAttrImpl extends BaseNodeImpl<StandaloneAttrImpl> implements Attr {
	/*
	 * @see org.w3c.dom.Attr#getName()
	 */
	@Override
	public String getName ()
	{
		return getNodeName();
	}

	public void setName (String name)
	{
		setNodeName(name);
	}
	/*
	 * @see org.w3c.dom.Attr#getValue()
	 */
	@Override
	public String getValue ()
	{
		return getNodeValue();
	}
	/*
	 * @see org.w3c.dom.Attr#setValue(java.lang.String)
	 */
	@Override
	public void setValue (String value) throws DOMException
	{
		setNodeValue(value);
	}

	private Element	_owner	/* =null */;
	/*
	 * @see org.w3c.dom.Attr#getOwnerElement()
	 */
	@Override
	public Element getOwnerElement ()
	{
		return _owner;
	}

	public void setOwnerElement (Element owner)
	{
		_owner = owner;
		setParentNode(owner);
	}

	public StandaloneAttrImpl (Element owner, String baseURI, String name, String value)
	{
		super(StandaloneAttrImpl.class, baseURI, name, value);

		_owner = owner;
	}

	public StandaloneAttrImpl (String baseURI, String name, String value)
	{
		this(null, baseURI, name, value);
	}

	public StandaloneAttrImpl (String name, String value)
	{
		this((String) null, name, value);
	}

	public StandaloneAttrImpl (Element owner, String name, String value)
	{
		this(owner, null, name, value);
	}

	public StandaloneAttrImpl (Element owner, String name)
	{
		this(owner, name, null);
	}

	public StandaloneAttrImpl (Element owner)
	{
		this(owner, null);
	}

	public StandaloneAttrImpl ()
	{
		this(null);
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

	private boolean	_specified=true;
	/* NOTE !!! default=TRUE
	 * @see org.w3c.dom.Attr#getSpecified()
	 */
	@Override
	public boolean getSpecified ()
	{
		return _specified;
	}
	// just to make it Java bean compliant
	public boolean isSpecified ()
	{
		return getSpecified();
	}

	public void setSpecified (boolean specified)
	{
		_specified = specified;
	}

	private boolean	_isId	/* =false */;
	/*
	 * @see org.w3c.dom.Attr#isId()
	 */
	@Override
	public boolean isId ()
	{
		return _isId;
	}

	public void setId (boolean isId)
	{
		_isId = isId;
	}
	/*
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	@Override
	public final /* no cheating */ short getNodeType ()
	{
		return Node.ATTRIBUTE_NODE;
	}
	/*
	 * @see org.w3c.dom.Node#getOwnerDocument()
	 */
	@Override
	public Document getOwnerDocument ()
	{
		final Element	elem=getOwnerElement();
		return (null == elem) ? super.getOwnerDocument() : elem.getOwnerDocument();
	}
}
