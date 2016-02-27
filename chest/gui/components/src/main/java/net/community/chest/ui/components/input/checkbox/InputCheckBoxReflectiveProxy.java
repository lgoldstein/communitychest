/*
 * 
 */
package net.community.chest.ui.components.input.checkbox;

import net.community.chest.ui.helpers.button.TypedCheckBoxReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> Type of {@link InputCheckBox} being reflected
 * @author Lyor G.
 * @since Jan 13, 2009 3:23:27 PM
 */
public class InputCheckBoxReflectiveProxy<B extends InputCheckBox> extends TypedCheckBoxReflectiveProxy<Boolean,B> {
	protected InputCheckBoxReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public InputCheckBoxReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final InputCheckBoxReflectiveProxy<InputCheckBox>	INPCB=
		new InputCheckBoxReflectiveProxy<InputCheckBox>(InputCheckBox.class, true);
}
