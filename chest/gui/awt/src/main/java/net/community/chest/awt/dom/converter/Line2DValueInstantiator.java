/*
 * 
 */
package net.community.chest.awt.dom.converter;

import java.awt.Rectangle;
import java.awt.geom.Line2D;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The reflected {@link Line2D} type
 * @author Lyor G.
 * @since Feb 2, 2009 1:55:06 PM
 */
public abstract class Line2DValueInstantiator<L extends Line2D> extends AbstractXmlValueStringInstantiator<L> {
	public Line2DValueInstantiator (Class<L> objClass) throws IllegalArgumentException
	{
		super(objClass);
	}

	public static final String toString (final Line2D l)
	{
		return RectangleValueInstantiator.toString((null == l) ? null : l.getBounds());
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (L inst) throws Exception
	{
		return toString(inst);
	}

	public static final <V extends Line2D> V fromString (V l, String s)
	{
		final Rectangle	r=
			(null == l) ? null : RectangleValueInstantiator.fromString(s);
		if (null == r)
			return l;

		final double	x=r.getX(), y=r.getY();
		l.setLine(x, y, x + r.getWidth(), y + r.getHeight());
		return l;
	}

	public static final Line2D fromString (final String v)
	{
		final String	s=StringUtil.getCleanStringValue(v);
		return fromString(((null == s) || (s.length() <= 0)) ? null : new Line2D.Double(), s);
	}

	public static final Line2DValueInstantiator<Line2D>	DEFAULT=
			new Line2DValueInstantiator<Line2D>(Line2D.class) {
				/*
				 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
				 */
				@Override
				public Line2D newInstance (String s) throws Exception
				{
					return fromString(s);
				}
		};
}
