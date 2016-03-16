/*
 *
 */
package net.community.chest.awt.layout.gridbag;

import java.awt.GridBagLayout;

import net.community.chest.awt.layout.dom.AbstractLayoutManager2ReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The {@link GridBagLayout} being reflected
 * @author Lyor G.
 * @since Aug 20, 2008 1:28:58 PM
 */
public class GridBagLayoutReflectiveProxy<L extends GridBagLayout> extends AbstractLayoutManager2ReflectiveProxy<L> {
    public GridBagLayoutReflectiveProxy (Class<L> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }

    public static final GridBagLayoutReflectiveProxy<GridBagLayout>    GRIDBAG=
            new GridBagLayoutReflectiveProxy<GridBagLayout>(GridBagLayout.class);
}
