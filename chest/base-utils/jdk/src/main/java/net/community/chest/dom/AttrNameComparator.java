package net.community.chest.dom;

import net.community.chest.lang.StringUtil;

import org.w3c.dom.Attr;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Compares 2 XML {@link Attr}-ibutes by their {@link Attr#getName()} value(s)</P>
 *
 * @param <A> The generic {@link Attr}-ibute class
 * @author Lyor G.
 * @since Nov 22, 2007 8:39:07 AM
 */
public class AttrNameComparator<A extends Attr> extends NodeNameComparator<A> {
    /**
     *
     */
    private static final long serialVersionUID = 1267113759423623293L;

    public AttrNameComparator (Class<A> nodeClass, boolean ascending, boolean caseSensitive)
    {
        super(nodeClass, ascending, caseSensitive);
    }

    public AttrNameComparator (Class<A> nodeClass, boolean ascending)
    {
        this(nodeClass, ascending, true);
    }

    public static final int compareNames (Attr a1, Attr a2, boolean caseSensitive)
    {
        final String    v1=(null == a1) ? null : a1.getName(),
                        v2=(null == a2) ? null : a2.getName();
        return StringUtil.compareDataStrings(v1, v2,  caseSensitive);
    }

    public int compareNames (A a1, A a2)
    {
        return compareNames(a1, a2, isCaseSensitive());
    }
    /*
     * @see net.community.chest.dom.NodeNameComparator#compareValues(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    @Override
    public int compareValues (A a1, A a2)
    {
        return compareNames(a1, a2);
    }

    public static final AttrNameComparator<Attr>    CASE_SENSITIVE_ATTR_NAME=new AttrNameComparator<Attr>(Attr.class, true, true),
                                                    CASE_INSENSITIVE_ATTR_NAME=new AttrNameComparator<Attr>(Attr.class, true, false);
}
