/*
 * 
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.ui.RectangleAnchor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 26, 2009 1:06:49 PM
 */
public class RectAnchorValueStringInstantiator extends AbstractXmlValueStringInstantiator<RectangleAnchor> {
	public RectAnchorValueStringInstantiator ()
	{
		super(RectangleAnchor.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (RectangleAnchor inst) throws Exception
	{
		if (null == inst)
			return null;

		final RectAnchor	e=RectAnchor.fromAnchor(inst);
		if (null == e)
			throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

		return e.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public RectangleAnchor newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final RectAnchor	e=RectAnchor.fromString(s);
		if (null == e)
			throw new NoSuchElementException("newInstance(" + s + ") unknown value");

		return e.getAnchor();
	}

	public static final RectAnchorValueStringInstantiator	DEFAULT=new RectAnchorValueStringInstantiator();
}
