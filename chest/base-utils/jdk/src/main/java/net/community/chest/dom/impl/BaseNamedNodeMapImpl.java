/*
 * 
 */
package net.community.chest.dom.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 17, 2009 10:02:26 AM
 */
public class BaseNamedNodeMapImpl extends TreeMap<String,Node> implements NamedNodeMap {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3051785546371424287L;
	public BaseNamedNodeMapImpl (Comparator<? super String> comparator)
	{
		super(comparator);
	}

	public BaseNamedNodeMapImpl ()
	{
		this(String.CASE_INSENSITIVE_ORDER);
	}

	public BaseNamedNodeMapImpl (Map<String,? extends Node> m)
	{
		this();
		putAll(m);
	}
	/*
	 * @see org.w3c.dom.NamedNodeMap#getLength()
	 */
	@Override
	public int getLength ()
	{
		return size();
	}
	/*
	 * @see org.w3c.dom.NamedNodeMap#getNamedItem(java.lang.String)
	 */
	@Override
	public Node getNamedItem (String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;
		return get(name);
	}

	protected String getNSItemKey (String namespaceURI, String localName) throws DOMException
	{
		final String	key=namespaceURI + ":" + localName;
		if (((null == namespaceURI) || (namespaceURI.length() <= 0))
		 && ((null == localName) || (localName.length() <= 0)))
		 	throw new DOMException(DOMException.NAMESPACE_ERR, "getNSItemKey(" + key + ") incomplete specification");

		return key;
	}
	/*
	 * @see org.w3c.dom.NamedNodeMap#getNamedItemNS(java.lang.String, java.lang.String)
	 */
	@Override
	public Node getNamedItemNS (String namespaceURI, String localName) throws DOMException
	{
		return getNamedItem(getNSItemKey(namespaceURI,localName));
	}
	// built by-demand, cleared on any add/remove/clear/clone call
	private List<Node>	_nodesList;
	public List<Node> getNodesList ()
	{
		return _nodesList;
	}
	// CAVEAT EMPTOR - if misused may cause inconsistencies in the implementation
	public void setNodesList (List<Node> nl)
	{
		_nodesList = nl;
	}

	public void clearNodesList ()
	{
		final List<? extends Node>	nl=getNodesList();
		if ((nl != null) && (nl.size() > 0))
			nl.clear();
	}
	/*
	 * @see java.util.TreeMap#clear()
	 */
	@Override
	public void clear ()
	{
		super.clear();
		clearNodesList();
	}
	/*
	 * @see java.util.TreeMap#clone()
	 */
	@Override
	@CoVariantReturn
	public BaseNamedNodeMapImpl clone ()
	{
		final BaseNamedNodeMapImpl	ret=getClass().cast(super.clone());
		final List<Node>			nl=ret.getNodesList(),	// clone the nodes list as well
									cl=
				((null == nl) || (nl.size() <= 0)) ? null : new ArrayList<Node>(nl);
		ret.setNodesList(cl); 
		return ret;
	}
	/*
	 * @see java.util.TreeMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Node put (String key, Node value)
	{
		clearNodesList();
		return super.put(key, value);
	}
	/*
	 * @see java.util.TreeMap#putAll(java.util.Map)
	 */
	@Override
	public void putAll (Map<? extends String,? extends Node> map)
	{
		super.putAll(map);
		clearNodesList();
	}
	/*
	 * @see java.util.TreeMap#remove(java.lang.Object)
	 */
	@Override
	public Node remove (Object key)
	{
		final Node	n=super.remove(key);
		if (n != null)
			clearNodesList();
		return n;
	}
	/*
	 * @see org.w3c.dom.NamedNodeMap#item(int)
	 */
	@Override
	public Node item (int index)
	{
		if ((index < 0) || (index >= getLength()))
			throw new DOMException(DOMException.INDEX_SIZE_ERR, "item(" + index + ") bad index");

		List<Node>	nl=getNodesList();
		if ((null == nl) || (nl.size() <= 0))
		{
			final Collection<? extends Node>	al=values();
			if ((al != null) && (al.size() > 0))	// should not be otherwise
			{
				if (null == nl)
				{
					nl = new ArrayList<Node>(al);
					setNodesList(nl);
				}
				else
					nl.addAll(al);
			}
		}

		if (index >= nl.size())
			throw new DOMException(DOMException.INVALID_STATE_ERR, "item(" + index + ") missing nodes");

		return nl.get(index);
	}
	/*
	 * @see org.w3c.dom.NamedNodeMap#removeNamedItem(java.lang.String)
	 */
	@Override
	public Node removeNamedItem (String name) throws DOMException
	{
		if ((null == name) || (name.length() <= 0))
			return null;
		return remove(name);
	}
	/*
	 * @see org.w3c.dom.NamedNodeMap#removeNamedItemNS(java.lang.String, java.lang.String)
	 */
	@Override
	public Node removeNamedItemNS (String namespaceURI, String localName) throws DOMException
	{
		return removeNamedItem(getNSItemKey(namespaceURI,localName));
	}
	/*
	 * @see org.w3c.dom.NamedNodeMap#setNamedItem(org.w3c.dom.Node)
	 */
	@Override
	public Node setNamedItem (Node n) throws DOMException
	{
		if (null == n)
			throw new DOMException(DOMException.VALIDATION_ERR, "setNamedItem() no node provided");

		return put(n.getNodeName(), n);
	}
	/*
	 * @see org.w3c.dom.NamedNodeMap#setNamedItemNS(org.w3c.dom.Node)
	 */
	@Override
	public Node setNamedItemNS (Node n) throws DOMException
	{
		final String	key=getNSItemKey((null == n) ? null : n.getNamespaceURI(), (null == n) ? null : n.getLocalName());
		return put(key, n);
	}
	/*
	 * @see java.util.AbstractMap#toString()
	 */
	@Override
	public String toString ()
	{
		return DOMUtils.toAttributesString(this);
	}
}
