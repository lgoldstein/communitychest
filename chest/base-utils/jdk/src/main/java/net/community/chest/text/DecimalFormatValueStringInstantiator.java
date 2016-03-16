/*
 *
 */
package net.community.chest.text;

import java.text.DecimalFormat;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> Type of instantiated {@link DecimalFormat}
 * @author Lyor G.
 * @since Feb 18, 2009 1:17:08 PM
 */
public abstract class DecimalFormatValueStringInstantiator<F extends DecimalFormat> extends AbstractXmlValueStringInstantiator<F> {
    protected DecimalFormatValueStringInstantiator (Class<F> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (F inst) throws Exception
    {
        return (null == inst) ? null : inst.toPattern();
    }

    public static final DecimalFormatValueStringInstantiator<DecimalFormat>    DEFAULT=
            new DecimalFormatValueStringInstantiator<DecimalFormat>(DecimalFormat.class) {
                /*
                 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
                 */
                @Override
                public DecimalFormat newInstance (String v) throws Exception
                {
                    final String    s=StringUtil.getCleanStringValue(v);
                    if ((null == s) || (s.length() <= 0))
                        return null;

                    return new DecimalFormat(s);
                }
            };
}
