/*
 *
 */
package net.community.chest.jfree.jfreechart;

import java.awt.Color;

import net.community.chest.awt.dom.converter.ColorValueInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <C> The instantiated {@link org.jfree.chart.ChartColor} type
 * @author Lyor G.
 * @since Jan 26, 2009 2:15:11 PM
 */
public abstract class ChartColorValueInstantiator<C extends Color> extends ColorValueInstantiator<C> {
    protected ChartColorValueInstantiator (Class<C> c)
    {
        super(c);
    }
    /*
     * @see net.community.chest.awt.dom.converter.ColorValueInstantiator#convertInstance(java.awt.Color)
     */
    @Override
    public String convertInstance (C inst) throws Exception
    {
        if (null == inst)
            return null;

        final ChartColors    cc=ChartColors.fromColor(inst);
        if (cc != null)
            return cc.toString();

        return super.convertInstance(inst);
    }

    public static final Color fromChartColorString (final String v) throws NumberFormatException
    {
        final String    s=StringUtil.getCleanStringValue(v);
        final int        sLen=(null == s) ? 0 : s.length();
        if (sLen <= 0)
            return null;

        final ChartColors    cc=ChartColors.fromString(s);
        if (cc != null)
            return cc.getChartColor();

        return fromColorString(s);
    }

    public static final ChartColorValueInstantiator<Color>    CHARTCOLOR=
        new ChartColorValueInstantiator<Color>(Color.class) {
            /*
             * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
             */
            @Override
            public Color newInstance (String s) throws Exception
            {
                return fromChartColorString(s);
            }
        };
}
