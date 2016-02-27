package net.community.chest.swing.component.menu;

import javax.swing.JMenuItem;

import net.community.chest.swing.component.button.AbstractButtonReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <MI> The reflected {@link JMenuItem} type
 * @author Lyor G.
 * @since Mar 20, 2008 8:46:13 AM
 */
public class JMenuItemReflectiveProxy<MI extends JMenuItem> extends AbstractButtonReflectiveProxy<MI> {
	public JMenuItemReflectiveProxy (Class<MI> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JMenuItemReflectiveProxy (Class<MI> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final JMenuItemReflectiveProxy<JMenuItem>	MENUITEM=
					new JMenuItemReflectiveProxy<JMenuItem>(JMenuItem.class, true);
}
