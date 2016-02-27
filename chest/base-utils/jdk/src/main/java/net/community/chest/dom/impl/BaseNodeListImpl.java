package net.community.chest.dom.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 13, 2007 2:42:51 PM
 */
public class BaseNodeListImpl extends ArrayList<Node> implements NodeList {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4630212496069064043L;
	public BaseNodeListImpl ()
	{
		super();
	}

	public BaseNodeListImpl (Collection<? extends Node> c)
	{
		super(c);
	}

	public BaseNodeListImpl (int initialCapacity)
	{
		super(initialCapacity);
	}
	/*
	 * @see org.w3c.dom.NodeList#getLength()
	 */
	@Override
	public int getLength ()
	{
		return size();
	}
	/*
	 * @see org.w3c.dom.NodeList#item(int)
	 */
	@Override
	public Node item (int index)
	{
		return get(index);
	}
}
