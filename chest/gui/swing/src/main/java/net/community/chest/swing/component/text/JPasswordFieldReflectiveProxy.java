/*
 * 
 */
package net.community.chest.swing.component.text;

import javax.swing.JPasswordField;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link JPasswordField}
 * @author Lyor G.
 * @since Sep 24, 2008 1:42:11 PM
 */
public class JPasswordFieldReflectiveProxy<F extends JPasswordField> extends JTextFieldReflectiveProxy<F> {
	public JPasswordFieldReflectiveProxy (Class<F> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JPasswordFieldReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final JPasswordFieldReflectiveProxy<JPasswordField>	PASSFIELD=
		new JPasswordFieldReflectiveProxy<JPasswordField>(JPasswordField.class, true);
}
