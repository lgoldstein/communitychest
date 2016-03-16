package net.community.chest.dom;

import net.community.chest.lang.StringUtil;

import org.w3c.dom.Node;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Takes into account also the {@link Node#getNodeValue()} (where name
 * takes precedence though). <B>Note:</B> case sensitivity applies to
 * <U>both</U> name <U>and</U> value</P>
 *
 * @param <N> Type of compared {@link Node}
 * @author Lyor G.
 * @since Nov 22, 2007 8:50:09 AM
 */
public class NodeDataComparator<N extends Node> extends NodeNameComparator<N> {
    /**
     *
     */
    private static final long serialVersionUID = 5329432053917260682L;

    public NodeDataComparator (Class<N> nodeClass, boolean ascending, boolean caseSensitive)
    {
        super(nodeClass, ascending, caseSensitive);
    }

    public NodeDataComparator (Class<N> nodeClass, boolean ascending)
    {
        this(nodeClass, ascending, true);
    }

    public static final int compareNodeValues (final Node n1, final Node n2, final boolean caseSensitive)
    {
        final String    v1=(null == n1) ? null : n1.getNodeValue(),
                        v2=(null == n2) ? null : n2.getNodeValue();

        return StringUtil.compareDataStrings(v1, v2, caseSensitive);
    }
    /*
     * @see net.community.chest.dom.NodeNameComparator#compareValues(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    @Override
    public int compareValues (N n1, N n2)
    {
        int    nRes=super.compare(n1, n2);
        if (0 == nRes)
            nRes = compareNodeValues(n1, n2, isCaseSensitive());
        return nRes;
    }

    public static final NodeDataComparator<Node>    CASE_SENSITIVE_NODE_DATA=new NodeDataComparator<Node>(Node.class, true, true),
                                                    CASE_INSENSITIVE_NODE_DATA=new NodeDataComparator<Node>(Node.class, true, false);
}
