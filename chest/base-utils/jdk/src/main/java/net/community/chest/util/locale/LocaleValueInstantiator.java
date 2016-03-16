/*
 *
 */
package net.community.chest.util.locale;

import java.util.Locale;
import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 16, 2008 10:14:35 AM
 */
public class LocaleValueInstantiator extends AbstractXmlValueStringInstantiator<Locale> {
    public LocaleValueInstantiator ()
    {
        super(Locale.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (final Locale inst) throws Exception
    {
        return (null == inst) ? null : LocaleUtils.getLocalePattern(inst);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Locale newInstance (String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final Locale    l=LocaleUtils.getFormattingLocale(s);
        if (null == l)
            throw new NoSuchElementException("Illegal format: " + s);

        return l;
    }

    public static final LocaleValueInstantiator    DEFAULT=new LocaleValueInstantiator();
}
