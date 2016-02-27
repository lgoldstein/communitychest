package net.community.chest.dom;

import net.community.chest.lang.StringUtil;

import org.w3c.dom.Node;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Compares 2 XML {@link Node}-s according to their {@link Node#getNodeName()}
 * value(s)</P>
 * 
 * @param <N> Type of compared {@link Node}
 * @author Lyor G.
 * @since Nov 22, 2007 8:31:32 AM
 */
public class NodeNameComparator<N extends Node> extends NodeComparator<N> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8445013469353715337L;
	/**
	 * TRUE if name comparison should be case sensitive (default)
	 */
	private boolean	_caseSensitive	/* =false */;
	public boolean isCaseSensitive ()
	{
		return _caseSensitive;
	}

	public void setCaseSensitive (boolean caseSensitive)
	{
		_caseSensitive = caseSensitive;
	}

	public NodeNameComparator (Class<N> nodeClass, boolean ascending, boolean caseSensitive)
	{
		super(nodeClass, !ascending);
		_caseSensitive = caseSensitive;
	}

	public NodeNameComparator (Class<N> nodeClass, boolean ascending)
	{
		this(nodeClass, ascending, true);
	}
	/*
	 * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (N n1, N n2)
	{
		final String	v1=(null == n1) ? null : n1.getNodeName(),
						v2=(null == n2) ? null : n2.getNodeName();
		return StringUtil.compareDataStrings(v1, v2, isCaseSensitive());
	}

	public static final NodeNameComparator<Node>	CASE_SENSITIVE_NODE_NAME=new NodeNameComparator<Node>(Node.class, true, true),
													CASE_INSENSITIVE_NODE_NAME=new NodeNameComparator<Node>(Node.class, true, false);
}
