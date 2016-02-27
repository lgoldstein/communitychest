package net.community.chest.dom;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Takes into account the attributes names and values as sub-comparison.
 * <B>Note:</B> case sensitivity applies for the element as well as its
 * attributes</P>
 * 
 * @param <E> Type of used {@link Element}
 * @author Lyor G.
 * @since Nov 22, 2007 8:29:28 AM
 */
public class ElementDataComparator<E extends Element> extends ElementNameComparator<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7666833183026528492L;
	public ElementDataComparator (Class<E> nodeClass, boolean ascending, boolean caseSensitive)
	{
		super(nodeClass, ascending, caseSensitive);
	}

	public ElementDataComparator (Class<E> nodeClass, boolean ascending)
	{
		this(nodeClass, ascending, true);
	}

	public Comparator<? super Attr> getAttributesComparator ()
	{
		return isCaseSensitive() ? AttrDataComparator.CASE_SENSITIVE_ATTR_DATA : AttrDataComparator.CASE_INSENSITIVE_ATTR_DATA;
	}

	public static final int compareAttributes (final NamedNodeMap al1, final NamedNodeMap al2, final Comparator<? super Attr> c)
	{
		final Collection<? extends Attr>	l1=DOMUtils.getNodeAttributesList(al1),
											l2=DOMUtils.getNodeAttributesList(al2);
		final int							nl1=(null == l1) ? 0 : l1.size(),
											nl2=(null == l2) ? 0 : l2.size();
		final Attr[]						aa1=(nl1 <= 0) ? null : l1.toArray(new Attr[nl1]),
											aa2=(nl2 <= 0) ? null : l2.toArray(new Attr[nl2]);
		final int							na1=(null == aa1) ? 0 : aa1.length,
											na2=(null == aa2) ? 0 : aa2.length,
											numCommon=Math.min(na1, na2);
		if (na1 > 1)
			Arrays.sort(aa1, c);
		if (na2 > 1)
			Arrays.sort(aa2, c);

		for (int	aIndex=0; aIndex < numCommon; aIndex++)
		{
			final Attr	a1=aa1[aIndex], a2=aa2[aIndex];
			final int	nRes=c.compare(a1, a2);
			if (nRes != 0)
				return nRes;
		}

		if (na1 != na2)	// just so we have a debug breakpoint
			return na1 - na2;	// the shorter number of attributes comes first

		return 0;
	}

	public int compareAttributes (final NamedNodeMap al1, final NamedNodeMap al2)
	{
		return compareAttributes(al1, al2, getAttributesComparator());
	}

	public int compareAttributes (E e1, E e2)
	{
		return compareAttributes((null == e1) ? null : e1.getAttributes(), (null == e2) ? null : e2.getAttributes());
	}
	/*
	 * @see net.community.chest.dom.ElementNameComparator#compareValues(org.w3c.dom.Element, org.w3c.dom.Element)
	 */
	@Override
	public int compareValues (E e1, E e2)
	{
		int	nRes=super.compareValues(e1, e2);
		if (0 == nRes)	// name takes precedence so check attributes only if same name
			nRes = compareAttributes(e1, e2);

		return nRes;
	}
}
