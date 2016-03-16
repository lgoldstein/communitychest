package net.community.chest.awt.dom.converter;

import java.awt.Color;

import net.community.chest.awt.Colors;
import net.community.chest.awt.SystemColors;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * Copyright 2007 as per GPLv2
 * @param <C> Type of {@link Color} being instantiated
 * @author Lyor G.
 * @since Jul 15, 2007 12:07:06 PM
 */
public abstract class ColorValueInstantiator<C extends Color>
        extends AbstractXmlValueStringInstantiator<C> {
    protected ColorValueInstantiator (Class<C> c)
    {
        super(c);
    }

    public static final Color fromColorString (final String v) throws NumberFormatException
    {
        final String    s=StringUtil.getCleanStringValue(v);
        final int        sLen=(null == s) ? 0 : s.length();
        if (sLen <= 0)
            return null;

        // check if one of the "known" default color names
        {
            final Colors    c=Colors.fromString(s);
            if (c != null)
                return c.getColor();
        }

        // check if one of the system colors names
        {
            final SystemColors    c=SystemColors.fromString(s);
            if (c != null)
                return c.getSystemColor();
        }

        return Color.decode(s);
    }

    public static final String toColorString (final Color c)
    {
        if (null == c)
            return null;

        final Colors    wc=Colors.fromColor(c);
        if (wc != null)
            return wc.toString();

        final SystemColors    sc=SystemColors.fromColor(c);
        if (sc != null)
            return sc.toString();

        return String.valueOf(c.getRGB());
    }
    /*
     * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (C inst) throws Exception
    {
        return toColorString(inst);
    }

    public static final ColorValueInstantiator<Color>    DEFAULT=
            new ColorValueInstantiator<Color>(Color.class) {
                /*
                 * @see net.community.chest.reflect.ValueStringInstantiator#newInstance(java.lang.String)
                 */
                @Override
                public Color newInstance (String s) throws Exception
                {
                    return fromColorString(s);
                }
            };
}
