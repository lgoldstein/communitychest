package net.community.chest.awt.dom.converter;

import java.awt.Point;
import java.util.List;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jul 16, 2007 2:47:01 PM
 */
public class PointValueInstantiator extends AbstractXmlValueStringInstantiator<Point> {
    public PointValueInstantiator ()
    {
        super(Point.class);
    }

    public static final String toString (final Point p)
    {
        if (null == p)
            return null;

        return new StringBuilder(64)
                                .append((int) Math.floor(p.getX() + 0.5))
                    .append(',').append((int) Math.floor(p.getY() + 0.5))
                    .toString();
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (Point inst) throws Exception
    {
        return toString(inst);
    }

    public static final Point fromString (final String v) throws NumberFormatException
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final List<String>    vals=StringUtil.splitString(s, ',');
        if ((null == vals) || (vals.size() < 2))
            throw new NumberFormatException("fromString(" + s + ") not enough components");

        final int    x=Integer.parseInt(StringUtil.getCleanStringValue(vals.get(0))),
                    y=Integer.parseInt(StringUtil.getCleanStringValue(vals.get(1)));
        return new Point(x, y);
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Point newInstance (String s) throws Exception
    {
        return fromString(s);
    }

    public static final PointValueInstantiator    DEFAULT=new PointValueInstantiator();
}
