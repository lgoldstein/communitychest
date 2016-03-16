/*
 *
 */
package net.community.chest.text;

import java.util.Currency;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 12, 2009 3:18:44 PM
 */
public class CurrencyValueInstantiator extends AbstractXmlValueStringInstantiator<Currency> {
    public CurrencyValueInstantiator ()
    {
        super(Currency.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (Currency inst) throws Exception
    {
        return (null == inst) ? null : inst.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Currency newInstance (String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        return ((null == s) || (s.length() <= 0)) ? null : Currency.getInstance(s);
    }

    public static final CurrencyValueInstantiator    DEFAULT=new CurrencyValueInstantiator();
}
