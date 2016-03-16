/*
 *
 */
package net.community.chest.awt.menu;

import java.awt.PopupMenu;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <M> The reflected {@link PopupMenu} instance
 * @author Lyor G.
 * @since Sep 7, 2008 4:01:16 PM
 */
public class PopupMenuReflectiveProxy<M extends PopupMenu> extends MenuReflectiveProxy<M> {
    public PopupMenuReflectiveProxy (Class<M> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }

    protected PopupMenuReflectiveProxy (Class<M> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final PopupMenuReflectiveProxy<PopupMenu>    POPUP=
        new PopupMenuReflectiveProxy<PopupMenu>(PopupMenu.class, true);
}
