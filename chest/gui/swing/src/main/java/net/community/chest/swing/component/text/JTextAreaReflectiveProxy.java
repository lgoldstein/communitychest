/*
 * 
 */
package net.community.chest.swing.component.text;

import javax.swing.JTextArea;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <A> The reflected {@link JTextArea} type
 * @author Lyor G.
 * @since Aug 28, 2008 12:17:09 PM
 */
public class JTextAreaReflectiveProxy<A extends JTextArea> extends JTextComponentReflectiveProxy<A> {
	public JTextAreaReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JTextAreaReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final JTextAreaReflectiveProxy<JTextArea>	TXTAREA=
			new JTextAreaReflectiveProxy<JTextArea>(JTextArea.class, true);
}
