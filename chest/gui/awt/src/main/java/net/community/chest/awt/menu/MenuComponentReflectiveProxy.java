/*
 *
 */
package net.community.chest.awt.menu;

import java.awt.MenuComponent;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <C> The reflected {@link MenuComponent} instance
 * @author Lyor G.
 * @since Sep 7, 2008 3:21:06 PM
 */
public class MenuComponentReflectiveProxy<C extends MenuComponent> extends UIReflectiveAttributesProxy<C> {
    protected MenuComponentReflectiveProxy (Class<C> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected MenuComponentReflectiveProxy (Class<C> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
}
