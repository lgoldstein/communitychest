/*
 *
 */
package net.community.chest.swing.component.toolbar;

import javax.swing.JToolBar;

import net.community.chest.swing.component.JSeparatorReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <S> The reflected {@link javax.swing.JToolBar.Separator} instance
 * @author Lyor G.
 * @since Sep 24, 2008 11:27:18 AM
 */
public class JToolBarSeparatorReflectiveProxy<S extends JToolBar.Separator> extends JSeparatorReflectiveProxy<S> {
    public JToolBarSeparatorReflectiveProxy (Class<S> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JToolBarSeparatorReflectiveProxy (Class<S> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final JToolBarSeparatorReflectiveProxy<JToolBar.Separator>    TBSEP=
        new JToolBarSeparatorReflectiveProxy<JToolBar.Separator>(JToolBar.Separator.class, true);
}
