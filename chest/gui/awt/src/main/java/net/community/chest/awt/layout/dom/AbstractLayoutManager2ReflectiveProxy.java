/*
 *
 */
package net.community.chest.awt.layout.dom;

import java.awt.LayoutManager2;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The {@link LayoutManager2} being reflected
 * @author Lyor G.
 * @since Aug 20, 2008 1:10:56 PM
 */
public abstract class AbstractLayoutManager2ReflectiveProxy<L extends LayoutManager2> extends AbstractLayoutManagerReflectiveProxy<L> {
    protected AbstractLayoutManager2ReflectiveProxy (Class<L> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }
}
