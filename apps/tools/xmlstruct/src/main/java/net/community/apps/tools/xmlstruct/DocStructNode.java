/*
 * 
 */
package net.community.apps.tools.xmlstruct;

import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.ui.components.tree.document.DOMNode;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <N> The type of {@link Node}
 * @author Lyor G.
 * @since Jan 6, 2009 4:19:39 PM
 */
public class DocStructNode<N extends Node> extends DOMNode<N> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4885008687183526564L;

	private static Collection<DocStructNode<?>> addNode (DocStructNode<?> rootNode, Collection<DocStructNode<?>> org, Node n)
	{
		Collection<DocStructNode<?>>	rl=org;
		if (null == n)
			return rl;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		final DocStructNode<? extends Node>	subNode=new DocStructNode(n.getClass(), n);
		rootNode.add(subNode);

		if (null == rl)
			rl = new LinkedList<DocStructNode<?>>();
		rl.add(subNode);

		final NamedNodeMap	am=n.getAttributes();
		final int			numAttrs=(null == am) ? 0 : am.getLength();
		for (int	aIndex=0; aIndex < numAttrs; aIndex++)
			rl = addNode(subNode, rl, am.item(aIndex));

		return rl;
	}

	private Collection<DocStructNode<?>> addNode (Collection<DocStructNode<?>> org, Node n)
	{
		return addNode(this, org, n);
	}

	public Collection<DocStructNode<?>> addSubElements (final N root)
	{
		final NodeList					nl=(null == root) ? null : root.getChildNodes();
		final int						numNodes=(null == nl) ? 0 : nl.getLength();
		Collection<DocStructNode<?>>	rl=null;
		for (int nIndex=0; nIndex < numNodes; nIndex++)
			rl = addNode(rl, nl.item(nIndex));

		return rl;
	}

	public DocStructNode (Class<N> nodeClass, N nodeObject, String nodeText, boolean withChildren)
	{
		super(nodeClass, nodeObject, nodeText, withChildren);

		if (withChildren)
			addSubElements(nodeObject);
	}

	public DocStructNode (Class<N> nodeClass, N nodeObject, boolean withChildren)
	{
		this(nodeClass, nodeObject, null, withChildren);
	}

	public DocStructNode (Class<N> nodeClass, N nodeObject, String nodeText)
	{
		this(nodeClass, nodeObject, nodeText, true);
	}

	public DocStructNode (Class<N> nodeClass, N nodeObject)
	{
		this(nodeClass, nodeObject, null);
	}

	public DocStructNode (Class<N> nodeClass)
	{
		this(nodeClass, null);
	}
}
