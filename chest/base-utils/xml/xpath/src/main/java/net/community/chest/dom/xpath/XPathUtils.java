/*
 * 
 */
package net.community.chest.dom.xpath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.set.SetsUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 6, 2009 2:36:43 PM
 */
public final class XPathUtils {
	private XPathUtils ()
	{
		// no instance
	}

	public static final Node removeElement (final Document doc, final Element elem) throws DOMException
	{
		if ((null == doc) || (null == elem))
			return doc;

		final Node	parent=elem.getParentNode();
		if (null == parent)
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "removeElement(" + DOMUtils.toString(elem) + ") no parent");
		return parent.removeChild(elem);
	}

	public static final List<Attr> removeAttributes (final Node n, final Comparator<? super String> c, final Collection<String> nl)
	{
		final int			numNames=(null == nl) ? 0 : nl.size(); 
		final NamedNodeMap	aMap=((null == n) ||  (numNames <= 0)) ? null : n.getAttributes();
		final int			numAttrs=(null == aMap) ? 0 : aMap.getLength();
		if (numAttrs <= 0)
			return null;

		List<Attr>	al=null;
		for (int	aIndex=0; aIndex < numNames; aIndex++)
		{
			final Node	na=aMap.item(aIndex);
			if ((null == na) || (na.getNodeType() != Node.ATTRIBUTE_NODE))
				continue;

			final Attr		a=(Attr) na;
			final String	an=a.getName();
			if (!CollectionsUtils.containsElement(nl, an, c))
				continue;
		
			if (null == al)
				al = new ArrayList<Attr>(numNames);
			al.add(a);
		}

		if ((null == al) || (al.size() <= 0))
			return null;

		for (final Attr a : al)
			n.removeChild(a);

		return al;
	}
	
	public static final List<Attr> removeAttributes (final Node n, final Comparator<? super String> c, final String ... nl)
	{
		return removeAttributes(n, null, (null == c) ? SetsUtils.comparableSetOf(nl) : SetsUtils.setOf(c, nl));
	}

	public static final List<Attr> removeAttributes (final Node n, final Collection<String> nl)
	{
		return removeAttributes(n, String.CASE_INSENSITIVE_ORDER, nl); 
	}

	public static final List<Attr> removeAttributes (final Node n, final String ... nl)
	{
		return removeAttributes(n, SetsUtils.setOf(String.CASE_INSENSITIVE_ORDER, nl));
	}
	// returns the newly created attribute - same as input if nothing created
	public static final Attr renameAttribute (final Document doc, final Element elem, final Attr a, final String nn)
	{
		final String	aName=(null == a) ? null : a.getName();
		if ((null == doc) || (null == elem) || (null == a)
		 || (null == nn) || (nn.length() <= 0)
		 || aName.equalsIgnoreCase(nn))	// ignore if already same name
			return a;

		final Attr	na=doc.createAttribute(nn);
		na.setValue(a.getValue());
		elem.removeChild(a);
		elem.appendChild(na);
		return na;
	}

	public static final List<Attr> insertAttributes (
			final Document doc, final Element elem, final Collection<? extends Map.Entry<String,String>> al)
	{
		final int	numAttrs=(null == al) ? 0 : al.size();
		if ((null == doc) || (null == elem) || (numAttrs <= 0))
			return null;

		List<Attr>	vl=null;
		for (final Map.Entry<String,String> ap : al)
		{
			final String	n=(null == ap) ? null : ap.getKey(),
							v=(null == ap) ? null : ap.getValue();
			if ((null == n) || (n.length() <= 0)
			 || (null == v) || (v.length() <= 0))
				continue;

			final Attr	a=doc.createAttribute(n);
			a.setValue(v);
			elem.appendChild(a);
			if (null == vl)
				vl = new ArrayList<Attr>(numAttrs);
			vl.add(a);
		}

		return vl;
	}

	public static final Node resolveInsertionParentNode (final Node n, final XPathInsertLocation pos)
	{
		if ((null == n) || (null == pos))
			return n;

		switch(pos)
		{
			case ABOVE	:
			case BEFORE	:	// TODO support these locations more correctly
				throw new UnsupportedOperationException("resolveInsertionParentNode(" + pos + ") N/A");
			case AFTER	:
				return n.getParentNode();
			case UNDER	:
				return n;

			default		:
				throw new NoSuchElementException("Unknown insertion position: " + pos);
		}
	}

	public static final Collection<Node> insertData (
			final Document				doc,
			final Element				parent,
			final Element				dataParent,
			final XPathInsertLocation	pos)
	{
		final NodeList	nl=(null == dataParent) ? null : dataParent.getChildNodes();
		final int		numNodes=(null == nl) ? 0 : nl.getLength();
		if (numNodes <= 0)	// debug breakpoint
			return null;

		final Node				parNode=resolveInsertionParentNode(parent, pos);
		final Collection<Node>	ret=new ArrayList<Node>(numNodes);
		for (int	nIndex=0; nIndex < numNodes; nIndex++)
		{
			final Node	n=nl.item(nIndex),
						an=(null == n) ? null : n.cloneNode(true),
						cn=(null == an) ? null : doc.adoptNode(an);
			if (null == cn)
				continue;

			parNode.appendChild(cn);
			ret.add(cn);
		}

		return ret;
	}
}
