/*
 * 
 */
package net.community.chest.swing.component.menu;

import javax.swing.JPopupMenu;

import net.community.chest.swing.component.JSeparatorReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <S> The reflected {@link javax.swing.JPopupMenu.Separator}
 * @author Lyor G.
 * @since Sep 24, 2008 11:32:08 AM
 */
public class JPopupMenuSeparatorReflectiveProxy<S extends JPopupMenu.Separator> extends JSeparatorReflectiveProxy<S> {
	public JPopupMenuSeparatorReflectiveProxy (Class<S> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JPopupMenuSeparatorReflectiveProxy (Class<S> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final JPopupMenuSeparatorReflectiveProxy<JPopupMenu.Separator>	PMSEP=
		new JPopupMenuSeparatorReflectiveProxy<JPopupMenu.Separator>(JPopupMenu.Separator.class, true);
}
