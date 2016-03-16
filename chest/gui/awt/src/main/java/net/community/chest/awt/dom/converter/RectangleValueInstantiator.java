package net.community.chest.awt.dom.converter;

import java.awt.Rectangle;
import java.util.List;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jul 15, 2007 12:26:48 PM
 */
public class RectangleValueInstantiator extends AbstractXmlValueStringInstantiator<Rectangle> {
    public RectangleValueInstantiator ()
    {
        super(Rectangle.class);
    }

    public static final <R extends Rectangle> R fromString (final String v, final R r) throws NumberFormatException
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == r) || (null == s) || (s.length() <= 0))
            return r;

        final List<String>    vals=StringUtil.splitString(s, ',');
        if ((null == vals) || (vals.size() < 4))
            throw new NumberFormatException("fromString(" + s + ") not enough components");

        final int    x=Integer.parseInt(vals.get(0)),
                    y=Integer.parseInt(vals.get(1)),
                    width=Integer.parseInt(vals.get(2)),
                    height=Integer.parseInt(vals.get(3));
        r.setBounds(x, y, width, height);
        return r;
    }

    public static final Rectangle fromString (final String v) throws NumberFormatException
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        return fromString(s, new Rectangle());
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Rectangle newInstance (final String s) throws Exception
    {
        return fromString(s);
    }

    public static final String toString (final Rectangle r)
    {
        if (null == r)
            return null;

        return new StringBuilder(64)
                        .append((int) Math.ceil(r.getX()))
            .append(',').append((int) Math.ceil(r.getY()))
            .append(',').append((int) Math.ceil(r.getWidth()))
            .append(',').append((int) Math.ceil(r.getHeight()))
            .toString()
            ;
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (Rectangle r) throws Exception
    {
        return toString(r);
    }

    public static final RectangleValueInstantiator    DEFAULT=new RectangleValueInstantiator();
}
