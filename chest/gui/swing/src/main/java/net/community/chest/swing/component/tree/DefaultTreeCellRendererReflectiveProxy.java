/*
 *
 */
package net.community.chest.swing.component.tree;

import java.lang.reflect.Method;

import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <R> The reflected {@link DefaultTreeCellRenderer} class
 * @author Lyor G.
 * @since Sep 4, 2008 8:58:30 AM
 */
public class DefaultTreeCellRendererReflectiveProxy<R extends DefaultTreeCellRenderer>
                extends TreeCellRendererReflectiveProxy<R> {
    public DefaultTreeCellRendererReflectiveProxy (Class<R> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected DefaultTreeCellRendererReflectiveProxy (Class<R> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final String    OPEN_ICON_ATTR="openIcon",
                                CLOSED_ICON_ATTR="closedIcon",
                                LEAF_ICON_ATTR="leafIcon";
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected R updateObjectAttribute (R src, String name, String value, Method setter) throws Exception
    {
        // some special resource related attributes
        if (OPEN_ICON_ATTR.equalsIgnoreCase(name)
         || CLOSED_ICON_ATTR.equalsIgnoreCase(name)
         || LEAF_ICON_ATTR.equalsIgnoreCase(name))
            return updateObjectResourceAttribute(src, name, value, setter);

        return super.updateObjectAttribute(src, name, value, setter);
    }

    public static final DefaultTreeCellRendererReflectiveProxy<DefaultTreeCellRenderer>    DEFAULT=
        new DefaultTreeCellRendererReflectiveProxy<DefaultTreeCellRenderer>(DefaultTreeCellRenderer.class, true);
}
