package net.community.chest.dom;

import net.community.chest.util.compare.AbstractComparator;

import org.w3c.dom.Node;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @param <N> Type of compared {@link Node}
 * @author Lyor G.
 * @since Nov 22, 2007 9:42:15 AM
 */
public abstract class NodeComparator<N extends Node> extends AbstractComparator<N> {
    /**
     *
     */
    private static final long serialVersionUID = 3215996084085367882L;

    protected NodeComparator (Class<N> objClass, boolean reverseMatch) throws IllegalArgumentException
    {
        super(objClass, reverseMatch);
    }
}
