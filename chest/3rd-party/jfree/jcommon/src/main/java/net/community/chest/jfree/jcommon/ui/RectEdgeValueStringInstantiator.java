/*
 * 
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.NoSuchElementException;

import org.jfree.ui.RectangleEdge;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 29, 2009 9:41:50 AM
 */
public class RectEdgeValueStringInstantiator extends AbstractXmlValueStringInstantiator<RectangleEdge> {
	public RectEdgeValueStringInstantiator ()
	{
		super(RectangleEdge.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (RectangleEdge inst) throws Exception
	{
		if (null == inst)
			return null;

		final RectEdge	e=RectEdge.fromEdge(inst);
		if (null == e)
			throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

		return e.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public RectangleEdge newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final RectEdge	e=RectEdge.fromString(s);
		if (null == e)
			throw new NoSuchElementException("newInstance(" + s + ") unknown value");

		return e.getEdge();
	}

	public static final RectEdgeValueStringInstantiator	DEFAULT=new RectEdgeValueStringInstantiator();
}
