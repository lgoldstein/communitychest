package net.community.chest.awt.dom.converter;

import java.awt.Dimension;
import java.util.List;
import java.util.Map;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.map.MapEntryImpl;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jul 15, 2007 12:30:41 PM
 */
public class DimensionValueInstantiator extends AbstractXmlValueStringInstantiator<Dimension> {
    public DimensionValueInstantiator ()
    {
        super(Dimension.class);
    }

    public static final Map.Entry<Integer,Integer> toIntPair (final String v) throws NumberFormatException
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final List<String>    vals=StringUtil.splitString(s, ',');
        if ((null == vals) || (vals.size() < 2))
            throw new NumberFormatException("toIntPair(" + s + ") not enough components");

        final int    width=Integer.parseInt(StringUtil.getCleanStringValue(vals.get(0))),
                    height=Integer.parseInt(StringUtil.getCleanStringValue(vals.get(1)));
        return new MapEntryImpl<Integer,Integer>(Integer.valueOf(width), Integer.valueOf(height));
    }

    public static final <D extends Dimension> D fromString (final String v, final D d) throws NumberFormatException
    {
        final Map.Entry<Integer,Integer>    np=toIntPair(v);
        if (null == np)
            return d;

        final Integer    width=np.getKey(), height=np.getValue();
        d.setSize(width.intValue(), height.intValue());
        return d;
    }

    public static final Dimension fromString (final String v) throws NumberFormatException
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        return fromString(s, new Dimension());
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Dimension newInstance (String s) throws Exception
    {
        return fromString(s);
    }

    public static final String toString (Dimension d)
    {
        if (null == d)
            return null;

        return new StringBuilder(64)
                        .append(d.width)
            .append(',').append(d.height)
            .toString()
            ;
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (Dimension d) throws Exception
    {
        return toString(d);
    }

    public static final DimensionValueInstantiator    DEFAULT=new DimensionValueInstantiator();
}
