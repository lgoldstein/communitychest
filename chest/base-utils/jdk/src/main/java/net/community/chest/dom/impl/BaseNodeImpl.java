package net.community.chest.dom.impl;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.CoVariantReturn;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @param <N> The {@link Node} instance being implemented
 * @author Lyor G.
 * @since Aug 7, 2007 10:06:35 AM
 */
public abstract class BaseNodeImpl<N extends Node> extends BaseTypedValuesContainer<N>
		implements Node, PubliclyCloneable<N> {
	private String	_baseURI	/* =null */;
	/*
	 * @see org.w3c.dom.Node#getBaseURI()
	 */
	@Override
	public String getBaseURI ()
	{
		return _baseURI;
	}

	public void setBaseURI (String baseURI)
	{
		_baseURI = baseURI;
	}

	private String	_name	/* =null */;
	/*
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	@Override
	public String getNodeName ()
	{
		return _name;
	}

	public void setNodeName (String name)
	{
		_name = name;
	}

	private String	_value	/* =null */;
	/*
	 * @see org.w3c.dom.Node#getNodeValue()
	 */
	@Override
	public String getNodeValue () throws DOMException
	{
		return _value;
	}

	@Override
	public void setNodeValue (String value)
	{
		_value = value;
	}

	protected BaseNodeImpl (Class<N> nodeClass, String baseURI, String name, String value)
	{
		super(nodeClass);

		_baseURI = baseURI;
		_name = name;
		_value = value;
	}
	
	protected BaseNodeImpl (Class<N> nodeClass, String name, String value)
	{
		this(nodeClass, null, name, value);
	}
	
	protected BaseNodeImpl (Class<N> nodeClass, String name)
	{
		this(nodeClass, name, null);
	}
	
	protected BaseNodeImpl (Class<N> nodeClass)
	{
		this(nodeClass, null);
	}

	private String	_lclName;
	/*
	 * @see org.w3c.dom.Node#getLocalName()
	 */
	@Override
	public String getLocalName ()
	{
		return _lclName;
	}

	public void setLocalName (String n)
	{
		_lclName = n;
	}

	private String	_nsURI;
	/*
	 * @see org.w3c.dom.Node#getNamespaceURI()
	 */
	@Override
	public String getNamespaceURI ()
	{
		return _nsURI;
	}

	public void setNamespaceURI (String n)
	{
		_nsURI = n;
	}
	/*
	 * @see org.w3c.dom.Node#getNextSibling()
	 */
	@Override
	public Node getNextSibling ()
	{
		return null;
	}
	/*
	 * @see org.w3c.dom.Node#getPrefix()
	 */
	@Override
	public String getPrefix ()
	{
		return getNamespaceURI();
	}
	/*
	 * @see org.w3c.dom.Node#setPrefix(java.lang.String)
	 */
	@Override
	public void setPrefix (String prefix) throws DOMException
	{
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getArgumentsExceptionLocation("setPrefix", prefix) + "  N/A");
	}
	/*
	 * @see org.w3c.dom.Node#cloneNode(boolean)
	 */
	@Override
	@CoVariantReturn
	public N cloneNode (boolean deep) throws DOMException
	{
		try
		{
			return clone();
		}
		catch(CloneNotSupportedException e)
		{
			throw new DOMException(DOMException.INVALID_STATE_ERR, getExceptionLocation("cloneNode") + " " + e.getClass().getName() + ": " + e);
		}
	}
	/*
	 * @see org.w3c.dom.Node#getPreviousSibling()
	 */
	@Override
	public Node getPreviousSibling ()
	{
		return null;
	}
	/*
	 * @see org.w3c.dom.Node#getTextContent()
	 */
	@Override
	public String getTextContent () throws DOMException
	{
		return getNodeValue();
	}
	/*
	 * @see org.w3c.dom.Node#getUserData(java.lang.String)
	 */
	@Override
	public Object getUserData (String key)
	{
		return null;
	}
	/*
	 * @see org.w3c.dom.Node#hasAttributes()
	 */
	@Override
	public boolean hasAttributes ()
	{
		return false;
	}
	/*
	 * @see org.w3c.dom.Node#hasChildNodes()
	 */
	@Override
	public boolean hasChildNodes ()
	{
		return false;
	}
	/*
	 * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	@Override
	public Node insertBefore (Node newChild, Node refChild) throws DOMException
	{
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getExceptionLocation("insertBefore") + " N/A");
	}
	/*
	 * @see org.w3c.dom.Node#isDefaultNamespace(java.lang.String)
	 */
	@Override
	public boolean isDefaultNamespace (String namespaceURI)
	{
		return (0 == StringUtil.compareDataStrings(getBaseURI(), namespaceURI, false));
	}
	/*
	 * @see org.w3c.dom.Node#isEqualNode(org.w3c.dom.Node)
	 */
	@Override
	public boolean isEqualNode (Node arg)
	{
		return (arg != null) && (arg.getNodeType() == getNodeType()) && equals(arg);
	}
	/*
	 * @see org.w3c.dom.Node#isSameNode(org.w3c.dom.Node)
	 */
	@Override
	public boolean isSameNode (Node other)
	{
		return isEqualNode(other);
	}
	/*
	 * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isSupported (String feature, String version)
	{
		return false;
	}
	/*
	 * @see org.w3c.dom.Node#lookupNamespaceURI(java.lang.String)
	 */
	@Override
	public String lookupNamespaceURI (String prefix)
	{
		return null;
	}
	/*
	 * @see org.w3c.dom.Node#lookupPrefix(java.lang.String)
	 */
	@Override
	public String lookupPrefix (String namespaceURI)
	{
		return null;
	}
	/*
	 * @see org.w3c.dom.Node#normalize()
	 */
	@Override
	public void normalize ()
	{
		// do nothing
	}
	/*
	 * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
	 */
	@Override
	public Node removeChild (Node oldChild) throws DOMException
	{
		return null;
	}
	/*
	 * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	@Override
	public Node replaceChild (Node newChild, Node oldChild) throws DOMException
	{
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getExceptionLocation("replaceChild") + " N/A");
	}
	/*
	 * @see org.w3c.dom.Node#setTextContent(java.lang.String)
	 */
	@Override
	public void setTextContent (String textContent) throws DOMException
	{
		setNodeValue(textContent);
	}
	/*
	 * @see org.w3c.dom.Node#setUserData(java.lang.String, java.lang.Object, org.w3c.dom.UserDataHandler)
	 */
	@Override
	public Object setUserData (String key, Object data, UserDataHandler handler) throws DOMException
	{
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getArgumentsExceptionLocation("setUserData", key, data) + " N/A");
	}
	/*
	 * @see org.w3c.dom.Node#compareDocumentPosition(org.w3c.dom.Node)
	 */
	@Override
	public short compareDocumentPosition (Node other) throws DOMException
	{
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getExceptionLocation("compareDocumentPosition") + " N/A");
	}

	protected NamedNodeMap createNamedNodeMap ()
	{
		return new BaseNamedNodeMapImpl();
	}

	private NamedNodeMap	_attrsMap	/* =null */;
	public NamedNodeMap getAttributes (boolean createIfNotExist)
	{
		if ((null == _attrsMap) && createIfNotExist)
			_attrsMap = createNamedNodeMap();

		return _attrsMap;
	}
	/*
	 * @see org.w3c.dom.Node#getAttributes()
	 */
	@Override
	public NamedNodeMap getAttributes ()
	{
		return getAttributes(false);
	}

	public void setAttributes (NamedNodeMap attrsMap)
	{
		_attrsMap = attrsMap;
	}

	private NodeList	_children	/* =null */;
	/*
	 * @see org.w3c.dom.Node#getChildNodes()
	 */
	@Override
	public NodeList getChildNodes ()
	{
		return _children;
	}
	/*
	 *  NOTE !!! if set NodeList implements the Collection interface then
	 *  "appendChild" will "add" to it, otherwise a DOMException will
	 *  be thrown 
	 */
	public void setChildNodes (NodeList l)
	{
		_children = l;
	}
	/*
	 * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
	 */
	@Override
	public synchronized Node appendChild (final Node newChild) throws DOMException
	{
		if (null == newChild)
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getExceptionLocation("appendChild") + " no child");

		if (null == _children)
			_children = new BaseNodeListImpl();
		if (_children instanceof Collection<?>)
		{
			@SuppressWarnings("unchecked")
			final Collection<Node>	cn=(Collection<Node>) _children;
			cn.add(newChild);
		}
		else
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR, getExceptionLocation("appendChild") + " unknown implementation: " + _children.getClass().getName());

		return newChild;
	}

	protected String getFeatureKey (String feature, String version)
	{
		return feature + "[" + version + "]";
	}

	private Map<String,Object>	_featuresMap;
	public Map<String,Object> getFeaturesMap ()
	{
		return _featuresMap;
	}

	public void setFeaturesMap (Map<String,Object> featuresMap)
	{
		_featuresMap = featuresMap;
	}

	public Object putFeature (String feature, String version, Object val)
	{
		final String				k=getFeatureKey(feature, version);
		Map<String,? super Object>	fm=getFeaturesMap();
		if (null == val)
		{
			if ((fm != null) && (fm.size() > 0))
				return fm.remove(k);

			return null;
		}
		else
		{
			if (null == fm)
			{
				setFeaturesMap(new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER));
				if (null == (fm=getFeaturesMap()))
					throw new DOMException(DOMException.INVALID_STATE_ERR, "putFeature(" + k + ") no map available");
			}

			return fm.put(k, val);
		}
	}
	/*
	 * @see org.w3c.dom.Node#getFeature(java.lang.String, java.lang.String)
	 */
	@Override
	public Object getFeature (String feature, String version)
	{
		final Map<String,?>	fm=getFeaturesMap();
		if ((null == fm) || (fm.size() <= 0))
			return null;

		final String	k=getFeatureKey(feature, version);
		return fm.get(k);
	}
	/*
	 * @see org.w3c.dom.Node#getFirstChild()
	 */
	@Override
	public Node getFirstChild ()
	{
		final NodeList	l=getChildNodes();
		final int		numNodes=(null == l) ? 0 : l.getLength();
		if (numNodes <= 0)
			return null;

		return l.item(0);
	}
	/*
	 * @see org.w3c.dom.Node#getLastChild()
	 */
	@Override
	public Node getLastChild ()
	{
		final NodeList	l=getChildNodes();
		final int		numNodes=(null == l) ? 0 : l.getLength();
		if (numNodes <= 0)
			return null;

		return l.item(numNodes - 1);
	}

	private Document	_owner	/* =null */;
	/*
	 * @see org.w3c.dom.Node#getOwnerDocument()
	 */
	@Override
	public Document getOwnerDocument ()
	{
		return _owner;
	}

	public void setOwnerDocument (Document owner)
	{
		_owner = owner;
	}

	private Node	_parent;
	/*
	 * @see org.w3c.dom.Node#getParentNode()
	 */
	@Override
	public Node getParentNode ()
	{
		return _parent;
	}

	public void setParentNode (Node p)
	{
		_parent = p;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public N clone () throws CloneNotSupportedException
	{
		// TODO clone children, attribute and features
		return getValuesClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if ((null == obj) || (!(obj instanceof Node)))
			return false;
		if (this == obj)
			return true;

		final Node	n=(Node) obj;
		return (0 == StringUtil.compareDataStrings(getBaseURI(), n.getBaseURI(), false))
			&& (0 == StringUtil.compareDataStrings(getNodeName(), n.getNodeName(), false))
			&& (0 == StringUtil.compareDataStrings(getNodeValue(), n.getNodeValue(), true))
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getBaseURI(), false)
			 + StringUtil.getDataStringHashCode(getNodeName(), false)
			 + StringUtil.getDataStringHashCode(getNodeValue(), true)
			 ;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getClass().getSimpleName() + "[" + getNodeName() + "]=" + getNodeValue();
	}
}
