/*
 * 
 */
package net.community.chest.awt.dom.proxy;

import javax.swing.JWindow;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <W> The reflected {@link JWindow} instance
 * @author Lyor G.
 * @since Dec 23, 2008 3:03:28 PM
 */
public class JWindowReflectiveProxy<W extends JWindow> extends WindowReflectiveProxy<W> {
	public JWindowReflectiveProxy (Class<W> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JWindowReflectiveProxy (Class<W> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final JWindowReflectiveProxy<JWindow>	JWINDOW=
			new JWindowReflectiveProxy<JWindow>(JWindow.class, true);
}
