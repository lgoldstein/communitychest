/*
 *
 */
package net.community.chest.util.locale;

import java.util.Locale;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Compares by country, language, variant (in this order) using case
 * <U>insensitive</I> comparison</P>
 *
 * @author Lyor G.
 * @since Dec 16, 2008 10:24:42 AM
 */
public class DefaultLocaleComparator extends AbstractComparator<Locale> {
    /**
     *
     */
    private static final long serialVersionUID = -8471068906773252071L;

    public DefaultLocaleComparator (boolean ascending)
    {
        super(Locale.class, !ascending);
    }
    /*
     * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (Locale v1, Locale v2)
    {
        // same order as LocaleUtils#getFormattingLocale
        final String[]    vals1={
                (null == v1) ? null : v1.getLanguage(),
                (null == v1) ? null : v1.getCountry(),
                (null == v1) ? null : v1.getVariant() },
                        vals2={
                (null == v2) ? null : v2.getLanguage(),
                (null == v2) ? null : v2.getCountry(),
                (null == v2) ? null : v2.getVariant() };
        for (int    vIndex=0; vIndex < vals1.length; vIndex++)
        {
            final String    s1=vals1[vIndex], s2=vals2[vIndex];
            final int        nRes=StringUtil.compareDataStrings(s1, s2, false);
            if (nRes != 0)
                return nRes;
        }

        return 0;
    }

    public static final DefaultLocaleComparator    ASCENDING=new DefaultLocaleComparator(true),
                                                DESCENDING=new DefaultLocaleComparator(false);
}
