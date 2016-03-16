package net.community.chest.dom;

import net.community.chest.lang.StringUtil;

import org.w3c.dom.Attr;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Compares 2 XML {@link Attr}-ibutes values as well as their names
 * (where the names take precedence). <B>Note:</B> case sensitivity applies
 * to <U>both</U> strings - name <U>and</U> value</P>
 *
 * @param <A> The generic {@link Attr}-ibute class
 * @author Lyor G.
 * @since Nov 22, 2007 8:41:24 AM
 */
public class AttrDataComparator<A extends Attr> extends AttrNameComparator<A> {
    /**
     *
     */
    private static final long serialVersionUID = 8932628985625385040L;

    public AttrDataComparator (Class<A> nodeClass, boolean ascending, boolean caseSensitive)
    {
        super(nodeClass, ascending, caseSensitive);
    }

    public AttrDataComparator (Class<A> nodeClass, boolean ascending)
    {
        this(nodeClass, ascending, true);
    }

    public static final int compareDataValues (Attr a1, Attr a2, boolean caseSensitive)
    {
        final String    v1=(null == a1) ? null : a1.getValue(),
                        v2=(null == a2) ? null : a2.getValue();

        return StringUtil.compareDataStrings(v1, v2, caseSensitive);
    }
    /*
     * @see net.community.chest.dom.AttrNameComparator#compareValues(org.w3c.dom.Attr, org.w3c.dom.Attr)
     */
    @Override
    public int compareValues (A a1, A a2)
    {
        int    nRes=super.compareValues(a1, a2);
        if (0 == nRes)
            nRes = compareDataValues(a1, a2, isCaseSensitive());

        return nRes;
    }

    public static final AttrDataComparator<Attr>    CASE_SENSITIVE_ATTR_DATA=new AttrDataComparator<Attr>(Attr.class, true, true),
                                                    CASE_INSENSITIVE_ATTR_DATA=new AttrDataComparator<Attr>(Attr.class, true, false);
}
