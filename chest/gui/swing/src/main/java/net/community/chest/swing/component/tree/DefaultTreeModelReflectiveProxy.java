/*
 *
 */
package net.community.chest.swing.component.tree;

import javax.swing.tree.DefaultTreeModel;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <M> The reflected {@link DefaultTreeModel}
 * @author Lyor G.
 * @since Aug 21, 2008 12:54:03 PM
 */
public class DefaultTreeModelReflectiveProxy<M extends DefaultTreeModel> extends TreeModelReflectiveProxy<M> {
    public DefaultTreeModelReflectiveProxy (Class<M> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected DefaultTreeModelReflectiveProxy (Class<M> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final DefaultTreeModelReflectiveProxy<DefaultTreeModel>    DEFMODEL=
        new DefaultTreeModelReflectiveProxy<DefaultTreeModel>(DefaultTreeModel.class, true);
}
