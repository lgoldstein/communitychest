/*
 *
 */
package net.community.chest.swing.component.tree;

import javax.swing.tree.TreeCellRenderer;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <R> The reflected {@link TreeCellRenderer} class
 * @author Lyor G.
 * @since Sep 4, 2008 8:57:04 AM
 */
public abstract class TreeCellRendererReflectiveProxy<R extends TreeCellRenderer> extends UIReflectiveAttributesProxy<R> {
    protected TreeCellRendererReflectiveProxy (Class<R> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected TreeCellRendererReflectiveProxy (Class<R> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

}
