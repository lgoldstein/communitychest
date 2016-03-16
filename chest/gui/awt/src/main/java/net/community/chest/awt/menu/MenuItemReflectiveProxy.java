/*
 *
 */
package net.community.chest.awt.menu;

import java.awt.MenuItem;
import java.awt.MenuShortcut;

import net.community.chest.convert.ValueStringInstantiator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <I> The reflected {@link MenuItem} type
 * @author Lyor G.
 * @since Sep 7, 2008 3:22:52 PM
 */
public class MenuItemReflectiveProxy<I extends MenuItem> extends MenuComponentReflectiveProxy<I> {
    public MenuItemReflectiveProxy (Class<I> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected MenuItemReflectiveProxy (Class<I> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if ((type != null) && MenuShortcut.class.isAssignableFrom(type))
            return (ValueStringInstantiator<C>) MenuShortcutValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }

    public static final MenuItemReflectiveProxy<MenuItem>    MENUITEM=
                new MenuItemReflectiveProxy<MenuItem>(MenuItem.class, true);
}
