/*
 * 
 */
package net.community.chest.awt.layout.border;

import java.awt.BorderLayout;
import java.util.NoSuchElementException;

import net.community.chest.awt.layout.dom.AbstractLayoutConstraintXmlValueInstantiator;
import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The {@link BorderLayout} type being used
 * @author Lyor G.
 * @since Jan 8, 2009 9:26:50 AM
 */
public class BorderLayoutXmlConstraintValueInstantiator<L extends BorderLayout>
		extends AbstractLayoutConstraintXmlValueInstantiator<L,String> {
	public BorderLayoutXmlConstraintValueInstantiator (Class<L> lmClass) throws IllegalStateException
	{
		super(lmClass, String.class);
	}

	public String fromXmlAttribute (final Attr a) throws Exception
	{
		if (null == a)
			return null;

		final String				aValue=a.getValue();
		final BorderLayoutPosition	pos=BorderLayoutPosition.fromString(aValue);
		if (null == pos)
			throw new NoSuchElementException("fromXmlAttribute(" + DOMUtils.toString(a) + ") unknown value");

		return pos.getPosition();
	}

	public final String	POSITION_ATTR="position";
	public boolean isPositionAttribute (final Attr a)
	{
		final String	aName=(null == a) ? null : a.getName();
		return POSITION_ATTR.equalsIgnoreCase(aName);
	}
	/*
	 * @see net.community.chest.awt.layout.dom.AbstractLayoutConstraintXmlValueInstantiator#fromXmlAttributes(org.w3c.dom.NamedNodeMap)
	 */
	@Override
	public String fromXmlAttributes (final NamedNodeMap attrs) throws Exception
	{
		final int	numAttrs=(null == attrs) ? 0 : attrs.getLength();
		for (int	aIndex=0; aIndex < numAttrs; aIndex++)
		{
			final Node	n=attrs.item(aIndex);
			if ((null == n) || (n.getNodeType() != Node.ATTRIBUTE_NODE))
				continue;

			final Attr	a=(Attr) n;
			if (!isPositionAttribute(a))
				continue;	// ignore any other attributes

			return fromXmlAttribute(a);
		}

		throw new IllegalArgumentException("No constraint attribute value found");
	}

	public static final BorderLayoutXmlConstraintValueInstantiator<BorderLayout>	BLCONST=
		new BorderLayoutXmlConstraintValueInstantiator<BorderLayout>(BorderLayout.class);
}
