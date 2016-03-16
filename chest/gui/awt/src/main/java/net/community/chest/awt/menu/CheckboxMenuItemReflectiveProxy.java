/*
 *
 */
package net.community.chest.awt.menu;

import java.awt.CheckboxMenuItem;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <I> The reflected {@link CheckboxMenuItem} instance
 * @author Lyor G.
 * @since Sep 7, 2008 3:58:28 PM
 */
public class CheckboxMenuItemReflectiveProxy<I extends CheckboxMenuItem> extends MenuItemReflectiveProxy<I> {
    public CheckboxMenuItemReflectiveProxy (Class<I> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected CheckboxMenuItemReflectiveProxy (Class<I> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final CheckboxMenuItemReflectiveProxy<CheckboxMenuItem>    CHECKBOX=
            new CheckboxMenuItemReflectiveProxy<CheckboxMenuItem>(CheckboxMenuItem.class, true);
}
