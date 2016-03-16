package net.community.chest.dom;

import net.community.chest.lang.StringUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Compares 2 XML {@link Element}-s names</P>
 *
 * @param <E> Type of compared {@link Element}
 * @author Lyor G.
 * @since Nov 22, 2007 8:24:49 AM
 */
public class ElementNameComparator<E extends Element> extends NodeNameComparator<E> {
    /**
     *
     */
    private static final long serialVersionUID = 8723207403310569423L;

    public ElementNameComparator (Class<E> nodeClass, boolean ascending, boolean caseSensitive)
    {
        super(nodeClass, ascending, caseSensitive);
    }

    public ElementNameComparator (Class<E> nodeClass, boolean ascending)
    {
        this(nodeClass, ascending, true);
    }

    public int compareTagName (E e1, E e2)
    {
        final String    n1=(null == e1) ? null : e1.getTagName(),
                        n2=(null == e2) ? null : e2.getTagName();
        return StringUtil.compareDataStrings(n1, n2, isCaseSensitive());
    }
    /*
     * @see net.community.chest.dom.NodeNameComparator#compareValues(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    @Override
    public int compareValues (E e1, E e2)
    {
        return compareTagName(e1, e2);
    }

    public static final ElementNameComparator<Element>    CASE_SENSITIVE_ELEMENT_NAME=new ElementNameComparator<Element>(Element.class, true, true),
                                                        CASE_INSENSITIVE_ELEMENT_NAME=new ElementNameComparator<Element>(Element.class, true, false);
}
