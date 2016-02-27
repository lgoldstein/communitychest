/*
 * 
 */
package net.community.chest.ui.components.tree.document;

import java.util.ArrayList;
import java.util.Collection;

import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 2:04:29 PM
 */
public class ElementNode extends DOMNode<Element> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6003665692761117551L;

	/*
	 * @see net.community.chest.swing.component.tree.TypedTreeNode#getNodeText(java.lang.Object)
	 */
	@Override
	public String getNodeText (final Element elem)
	{
		return (null == elem) ? null : elem.getTagName(); 
	}

	public Collection<ElementNode> addSubElements (final Element root)
	{
		final Collection<? extends Element>	el=
			DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
		final int							numElems=
			(null == el) ? 0 : el.size();
		if (numElems <= 0)
			return null;

		Collection<ElementNode>	nl=null;
		for (final Element elem : el)
		{
			final ElementNode	n=(null == elem) ? null : new ElementNode(elem, true);
			if (null == n)
				continue;

			add(n);
			if (null == nl)
				nl = new ArrayList<ElementNode>(numElems);
			nl.add(n);
		}

		return nl;
	}

	public ElementNode (Element nodeObject, String nodeText, boolean withChildren)
	{
		super(Element.class, nodeObject, nodeText, withChildren);

		if (withChildren)
			addSubElements(nodeObject);
	}

	public ElementNode (Element nodeObject, boolean withChildren)
	{
		this(nodeObject, null, withChildren);
	}

	public ElementNode (Element nodeObject, String nodeText)
	{
		this(nodeObject, nodeText, true);
	}

	public ElementNode (Element nodeObject)
	{
		this(nodeObject, null);
	}

	public ElementNode ()
	{
		this(null);
	}

	public final Element getElement ()
	{
		return getUserObject();
	}
}
